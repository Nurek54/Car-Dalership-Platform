package salon.logistics.application;

import salon.logistics.application.port.in.ManageInventoryUseCase;
import salon.logistics.application.port.out.ImporterIdentityAclPort;
import salon.logistics.application.port.out.ProductionSlotRepository;
import salon.logistics.application.port.out.VehicleRepository;
import salon.logistics.domain.model.slot.ProductionSlot;
import salon.logistics.domain.model.vehicle.ImporterData;
import salon.logistics.domain.model.vehicle.InventoryVehicle;
import salon.logistics.domain.model.vehicle.VinNumber;
import salon.logistics.domain.service.VehicleAllocationDomainService;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;
import salon.shared.model.OrderId;

import java.util.List;
import java.util.Optional;

/**
 * Orkiestracja kontekstu Inwentarza i Logistyki. Konsoliduje role z diagramu
 * (YardManagement / Allocation) w jeden serwis aplikacyjny, bo testy adapterów
 * odwołują się do salon.logistics.application.InventoryAppService.
 */
public class InventoryAppService implements ManageInventoryUseCase {

    private final VehicleRepository vehicleRepository;
    private final ProductionSlotRepository slotRepository;
    private final ImporterIdentityAclPort importerIdentity;
    private final VehicleAllocationDomainService allocationService;
    private final EventPublisherPort eventPublisher;

    public InventoryAppService(VehicleRepository vehicleRepository,
                               ProductionSlotRepository slotRepository,
                               ImporterIdentityAclPort importerIdentity,
                               VehicleAllocationDomainService allocationService,
                               EventPublisherPort eventPublisher) {
        if (vehicleRepository == null) {
            throw new IllegalArgumentException("vehicleRepository must not be null.");
        }
        if (slotRepository == null) {
            throw new IllegalArgumentException("slotRepository must not be null.");
        }
        if (importerIdentity == null) {
            throw new IllegalArgumentException("importerIdentity must not be null.");
        }
        if (allocationService == null) {
            throw new IllegalArgumentException("allocationService must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        this.vehicleRepository = vehicleRepository;
        this.slotRepository = slotRepository;
        this.importerIdentity = importerIdentity;
        this.allocationService = allocationService;
        this.eventPublisher = eventPublisher;
    }

    // UC-INW-01: przyjęcie auta na plac (z weryfikacją ACL).
    @Override
    public void receiveVehicle(String vin) {
        if (vin == null || vin.isBlank()) {
            throw new IllegalArgumentException("vin must not be blank.");
        }
        VinNumber vinNumber = new VinNumber(vin);
        Optional<InventoryVehicle> found = vehicleRepository.findByVin(vinNumber);
        InventoryVehicle vehicle = found.orElseGet(() -> new InventoryVehicle(vinNumber));
        ImporterData data = importerIdentity.verifyVin(vin);
        vehicle.receiveOnYard(data);
        vehicleRepository.save(vehicle);
        publishEventsOf(vehicle);
    }

    // UC-INW-07: Fast Track (blokada auta) albo Long Track (slot produkcyjny).
    @Override
    public void allocateVehicleForOrder(String orderId, List<String> specCodes) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        OrderId id = new OrderId(orderId);
        List<InventoryVehicle> all = vehicleRepository.findAll();

        boolean locked = allocationService.tryLockExistingVehicle(id, all, specCodes);
        if (locked) {
            for (int i = 0; i < all.size(); i++) {
                vehicleRepository.save(all.get(i));
            }
            return;
        }

        ProductionSlot slot = allocationService.createProductionSlot(id, specCodes);
        slotRepository.save(slot);
    }

    private void publishEventsOf(InventoryVehicle vehicle) {
        List<DomainEvent> events = vehicle.pullDomainEvents();
        for (int i = 0; i < events.size(); i++) {
            eventPublisher.publish(events.get(i));
        }
    }
}
