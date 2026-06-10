package salon.logistics.application;

import salon.logistics.application.port.in.ManageYardUseCase;
import salon.logistics.application.port.out.ImporterIdentityAclPort;
import salon.logistics.application.port.out.VehicleRepository;
import salon.logistics.domain.model.vehicle.ImporterData;
import salon.logistics.domain.model.vehicle.InventoryVehicle;
import salon.logistics.domain.model.vehicle.VinNumber;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;
import salon.shared.model.OrderId;

import java.util.List;
import java.util.Optional;

/**
 * Usługa aplikacyjna placu i PDI (UC-INW-01, 03, 05) —
 * węzeł "YardManagementAppService" w docs/Inwentarz-Logistyka/LogisticsArchitecture.md.
 */
public class YardManagementAppService implements ManageYardUseCase {

    private final VehicleRepository vehicleRepository;
    private final ImporterIdentityAclPort importerIdentity;
    private final EventPublisherPort eventPublisher;

    public YardManagementAppService(VehicleRepository vehicleRepository,
                                    ImporterIdentityAclPort importerIdentity,
                                    EventPublisherPort eventPublisher) {
        if (vehicleRepository == null) {
            throw new IllegalArgumentException("vehicleRepository must not be null.");
        }
        if (importerIdentity == null) {
            throw new IllegalArgumentException("importerIdentity must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        this.vehicleRepository = vehicleRepository;
        this.importerIdentity = importerIdentity;
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

    // UC-INW-03: dostawa na stock — jeśli czeka zamówienie, auto jest od razu rezerwowane.
    @Override
    public void registerDelivery(String vin) {
        if (vin == null || vin.isBlank()) {
            throw new IllegalArgumentException("vin must not be blank.");
        }
        VinNumber vinNumber = new VinNumber(vin);
        InventoryVehicle vehicle = vehicleRepository.findByVin(vinNumber)
                .orElseThrow(() -> new IllegalStateException("No vehicle on stock with VIN " + vin));

        Optional<OrderId> pendingOrder = vehicleRepository.findPendingOrderForSpec(vinNumber);
        if (pendingOrder.isEmpty()) {
            // A1: auto zamówione "na stock" — pozostaje ON_STOCK (wolne), bez zdarzenia.
            vehicleRepository.save(vehicle);
            return;
        }
        vehicle.lockForOrder(pendingOrder.get());
        vehicleRepository.save(vehicle);
        publishEventsOf(vehicle);
    }

    // UC-INW-05: SettlementCompleted -> auto gotowe do wydania (pozostaje RESERVED).
    @Override
    public void markVehicleReadyForHandover(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        InventoryVehicle vehicle = vehicleRepository.findByOrderId(new OrderId(orderId))
                .orElseThrow(() -> new IllegalStateException(
                        "No reserved vehicle for order " + orderId));
        vehicle.markReadyForHandover();
        vehicleRepository.save(vehicle);
        publishEventsOf(vehicle);
    }

    private void publishEventsOf(InventoryVehicle vehicle) {
        List<DomainEvent> events = vehicle.pullDomainEvents();
        for (int i = 0; i < events.size(); i++) {
            eventPublisher.publish(events.get(i));
        }
    }
}
