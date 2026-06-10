package salon.logistics.application;

import salon.logistics.application.port.in.HandleOrderEventsUseCase;
import salon.logistics.application.port.out.ProductionSlotRepository;
import salon.logistics.application.port.out.SpecificationIntegrationPort;
import salon.logistics.application.port.out.VehicleRepository;
import salon.logistics.domain.event.VehicleInventoryReleasedError;
import salon.logistics.domain.event.VehicleIsNotOnStockEvent;
import salon.logistics.domain.exceptions.InvalidVehicleStateException;
import salon.logistics.domain.model.slot.ProductionSlot;
import salon.logistics.domain.model.vehicle.InventoryVehicle;
import salon.logistics.domain.model.vehicle.VinNumber;
import salon.logistics.domain.service.VehicleAllocationDomainService;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;
import salon.shared.model.OrderId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Usługa aplikacyjna rezerwacji i zdarzeń zamówień (UC-INW-04, 06, 07) —
 * węzeł "AllocationAppService" w docs/Inwentarz-Logistyka/LogisticsArchitecture.md.
 */
public class AllocationAppService implements HandleOrderEventsUseCase {

    private final VehicleRepository vehicleRepository;
    private final ProductionSlotRepository slotRepository;
    private final SpecificationIntegrationPort specificationIntegration;
    private final VehicleAllocationDomainService allocationService;
    private final EventPublisherPort eventPublisher;

    public AllocationAppService(VehicleRepository vehicleRepository,
                                ProductionSlotRepository slotRepository,
                                SpecificationIntegrationPort specificationIntegration,
                                VehicleAllocationDomainService allocationService,
                                EventPublisherPort eventPublisher) {
        if (vehicleRepository == null) {
            throw new IllegalArgumentException("vehicleRepository must not be null.");
        }
        if (slotRepository == null) {
            throw new IllegalArgumentException("slotRepository must not be null.");
        }
        if (specificationIntegration == null) {
            throw new IllegalArgumentException("specificationIntegration must not be null.");
        }
        if (allocationService == null) {
            throw new IllegalArgumentException("allocationService must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        this.vehicleRepository = vehicleRepository;
        this.slotRepository = slotRepository;
        this.specificationIntegration = specificationIntegration;
        this.allocationService = allocationService;
        this.eventPublisher = eventPublisher;
    }

    // UC-INW-07: Fast Track (blokada auta z placu, -> VehicleReservedFromStockEvent)
    // albo Long Track (slot produkcyjny, -> VehicleIsNotOnStockEvent).
    @Override
    public void allocateVehicleForOrder(String orderId, List<String> specCodes) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        OrderId id = new OrderId(orderId);

        // UC-INW-02 (sekwencja): zdarzenie może nieść tylko orderId — wtedy kody wyposażenia
        // dociągamy z Kontekstu Katalogu/Sprzedaży (źródło prawdy o specyfikacji).
        List<String> codes = (specCodes == null || specCodes.isEmpty())
                ? specificationIntegration.getSpecificationForOrder(id)
                : specCodes;
        if (codes == null || codes.isEmpty()) {
            throw new IllegalStateException("No approved specification for order " + orderId);
        }

        List<InventoryVehicle> all = vehicleRepository.findAll();

        boolean locked = allocationService.tryLockExistingVehicle(id, all, codes);
        if (locked) {
            for (int i = 0; i < all.size(); i++) {
                vehicleRepository.save(all.get(i));
                publishEventsOf(all.get(i));
            }
            return;
        }

        ProductionSlot slot = allocationService.createProductionSlot(id, codes);
        slotRepository.save(slot);
        eventPublisher.publish(new VehicleIsNotOnStockEvent(
                UUID.randomUUID(), orderId, Instant.now()));
    }

    // UC-INW-04 (sekwencja): PaymentDeadlineExpired -> zwolnienie rezerwacji (idempotentne).
    @Override
    public void releaseReservationForOrder(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        Optional<InventoryVehicle> found = vehicleRepository.findByOrderId(new OrderId(orderId));
        if (found.isEmpty()) {
            // A1: pojazd wydany/usunięty — brak akcji (obsługa idempotentna).
            return;
        }
        InventoryVehicle vehicle = found.get();
        vehicle.releaseReservation();
        vehicleRepository.save(vehicle);
        publishEventsOf(vehicle);
    }

    // UC-INW-06: wydanie pojazdu klientowi (RESERVED -> HANDED_OVER).
    @Override
    public void releaseVehicle(String vin) {
        if (vin == null || vin.isBlank()) {
            throw new IllegalArgumentException("vin must not be blank.");
        }
        InventoryVehicle vehicle = vehicleRepository.findByVin(new VinNumber(vin))
                .orElseThrow(() -> new IllegalStateException("No vehicle with VIN " + vin));
        try {
            vehicle.handOver();
        } catch (InvalidVehicleStateException e) {
            // A1: niewłaściwy status (≠ gotowy do wydania) -> zdarzenie błędu i odrzucenie komendy.
            eventPublisher.publish(new VehicleInventoryReleasedError(
                    UUID.randomUUID(), vin, e.getMessage(), Instant.now()));
            throw e;
        }
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
