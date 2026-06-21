package salon.logistics.application.service;

import salon.common.application.EventPublisher;
import salon.common.model.OrderId;
import salon.logistics.application.domain.event.FactoryOrderFailedEvent;
import salon.logistics.application.domain.event.FactoryOrderPlacedEvent;
import salon.logistics.application.domain.event.VehicleDeliveredToStockEvent;
import salon.logistics.application.domain.event.VehicleInventoryReleasedErrorEvent;
import salon.logistics.application.domain.event.VehicleInventoryReleasedEvent;
import salon.logistics.application.domain.event.VehicleIsNotOnStockEvent;
import salon.logistics.application.domain.event.VehicleReadyForHandoverEvent;
import salon.logistics.application.domain.event.VehicleReservationCancelledEvent;
import salon.logistics.application.domain.event.VehicleReservedFromStockEvent;
import salon.logistics.application.domain.exception.FactoryOrderFailedException;
import salon.logistics.application.domain.exception.IllegalVehicleStateException;
import salon.logistics.application.domain.exception.VehicleNotFoundException;
import salon.logistics.application.domain.model.vehicle.ImporterData;
import salon.logistics.application.domain.model.vehicle.InventoryVehicle;
import salon.logistics.application.domain.model.vehicle.InventoryVehicleFactory;
import salon.logistics.application.domain.model.vehicle.SpecificationId;
import salon.logistics.application.domain.model.vehicle.VehicleState;
import salon.logistics.application.domain.model.vehicle.VinNumber;
import salon.logistics.application.port.in.PrepareForHandover;
import salon.logistics.application.port.in.ReceiveVehicle;
import salon.logistics.application.port.in.ReleaseVehicle;
import salon.logistics.application.port.in.ReserveVehicle;
import salon.logistics.application.port.out.CatalogIntegration;
import salon.logistics.application.port.out.ImporterACL;
import salon.logistics.application.port.out.VehicleDatabaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * APPLICATION SERVICE (Figure 37) – "InventoryManagementService".
 *
 * The single orchestration point of the Inventory and Logistics Context. Implements four inbound ports
 * z diagramu architektury: {@link ReserveVehicle}, {@link ReceiveVehicle}, {@link ReleaseVehicle}
 * and {@link PrepareForHandover} (covering UC-INW-01..06), using four outbound ports:
 * {@link VehicleDatabaseRepository}, {@link CatalogIntegration}, {@link ImporterACL} and the shared
 * {@link EventPublisher}. NON-BUSINESS operations (fetching the aggregate, saving, emitting events) are here;
 * the vehicle life-cycle rules — in the {@link InventoryVehicle} aggregate.
 */
public class InventoryManagementService implements
        ReserveVehicle, ReceiveVehicle, ReleaseVehicle, PrepareForHandover {

    private final VehicleDatabaseRepository vehicleRepository;
    private final CatalogIntegration catalogIntegration;
    private final ImporterACL importerAcl;
    private final EventPublisher eventPublisher;
    private final InventoryVehicleFactory vehicleFactory = new InventoryVehicleFactory();

    public InventoryManagementService(VehicleDatabaseRepository vehicleRepository,
                                      CatalogIntegration catalogIntegration,
                                      ImporterACL importerAcl,
                                      EventPublisher eventPublisher) {
        if (vehicleRepository == null) {
            throw new IllegalArgumentException("vehicleRepository must not be null.");
        }
        if (catalogIntegration == null) {
            throw new IllegalArgumentException("catalogIntegration must not be null.");
        }
        if (importerAcl == null) {
            throw new IllegalArgumentException("importerAcl must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        this.vehicleRepository = vehicleRepository;
        this.catalogIntegration = catalogIntegration;
        this.importerAcl = importerAcl;
        this.eventPublisher = eventPublisher;
    }

    // ===== Feeding the local copy of the Catalog data (CatalogIntegration outbound port) =====
    // Helper methods called by the composition root / demo; event subscribers may also
    // write directly to the CatalogIntegration port.

    public void registerSpecification(String specificationId, List<String> optionCodes) {
        this.catalogIntegration.saveSpecification(specificationId, optionCodes);
    }

    public void linkOrderToSpecification(String orderId, String specificationId) {
        this.catalogIntegration.linkOrderToSpecification(orderId, specificationId);
    }

    // ===== ReserveVehicle: UC-INW-01 (reservation from the yard) + UC-INW-02 (production order) =====

    @Override
    public void reserveVehicleForOrder(String orderId) {
        OrderId order = new OrderId(orderId);
        Optional<String> specificationId = this.catalogIntegration.findSpecificationByOrder(orderId);

        Optional<InventoryVehicle> free = this.vehicleRepository.findAll().stream()
                .filter(v -> v.state() == VehicleState.ON_STOCK && v.order() == null)
                .filter(v -> matchesSpecification(v, specificationId.orElse(null)))
                .findFirst();

        if (free.isPresent()) {
            InventoryVehicle vehicle = free.get();
            vehicle.lockForOrder(order);                    // ON_STOCK -> RESERVED (rule in the aggregate)
            this.vehicleRepository.save(vehicle);
            this.eventPublisher.publish(
                    new VehicleReservedFromStockEvent(orderId, vehicle.vin().value()));
        } else {
            // A1: no free car in the yard -> the production-order / deposit path.
            this.eventPublisher.publish(new VehicleIsNotOnStockEvent(orderId));
        }
    }

    @Override
    public void orderVehicleFromFactory(String orderId) {
        String specificationId = this.catalogIntegration.findSpecificationByOrder(orderId).orElse(null);
        List<String> optionCodes = specificationId == null
                ? List.of()
                : this.catalogIntegration.findOptionCodes(specificationId);
        try {
            String vin = this.importerAcl.placeFactoryOrder(orderId, optionCodes);
            InventoryVehicle vehicle = this.vehicleFactory.createForFactoryOrder(
                    new VinNumber(vin),
                    new OrderId(orderId),
                    specificationId == null ? null : new SpecificationId(specificationId));
            this.vehicleRepository.save(vehicle);
            this.eventPublisher.publish(new FactoryOrderPlacedEvent(orderId, vin));
        } catch (FactoryOrderFailedException e) {
            this.eventPublisher.publish(new FactoryOrderFailedEvent(orderId, e.getMessage()));
        }
    }

    // ===== ReceiveVehicle: UC-INW-03 (receiving the vehicle into stock) =====

    @Override
    public void receiveVehicle(String vin) {
        VinNumber vinNumber = new VinNumber(vin);
        Optional<InventoryVehicle> existing = this.vehicleRepository.findByVin(vinNumber);

        if (existing.isPresent()) {
            InventoryVehicle vehicle = existing.get();
            ImporterData data = new ImporterData(vinNumber, vehicle.specification(), List.of());
            vehicle.receiveOnYard(data);                    // IN_PRODUCTION -> RESERVED
            this.vehicleRepository.save(vehicle);
            OrderId order = vehicle.order();
            this.eventPublisher.publish(new VehicleDeliveredToStockEvent(
                    order == null ? null : order.value(), vin));
        } else {
            // A1: a "stock" car without an order -> registered as free, without an event.
            ImporterData data = new ImporterData(vinNumber, null, List.of());
            this.vehicleRepository.save(this.vehicleFactory.createStockArrival(data));
        }
    }

    // ===== ReleaseVehicle: UC-INW-06 (handover) + UC-INW-04 (lock release) =====

    @Override
    public void releaseVehicle(String orderId) {
        InventoryVehicle vehicle = this.vehicleRepository.findByOrderId(new OrderId(orderId))
                .orElseThrow(() -> new VehicleNotFoundException(
                        "No vehicle to hand over for order " + orderId));
        try {
            vehicle.handOver();                              // READY_FOR_HANDOVER -> HANDED_OVER
            this.vehicleRepository.save(vehicle);
            this.eventPublisher.publish(
                    new VehicleInventoryReleasedEvent(orderId, vehicle.vin().value()));
        } catch (IllegalVehicleStateException e) {
            // A1: incorrect vehicle status -> a compensating event for Sales.
            this.eventPublisher.publish(new VehicleInventoryReleasedErrorEvent(orderId, e.getMessage()));
        }
    }

    @Override
    public void releaseReservation(String orderId) {
        Optional<InventoryVehicle> reserved = this.vehicleRepository.findByOrderId(new OrderId(orderId));
        if (reserved.isEmpty()) {
            // A1: the vehicle does not exist among the reservations (previously removed/handed over) — nothing to do.
            return;
        }
        InventoryVehicle vehicle = reserved.get();
        vehicle.releaseReservation();                        // RESERVED -> ON_STOCK
        this.vehicleRepository.save(vehicle);
        this.eventPublisher.publish(
                new VehicleReservationCancelledEvent(orderId, vehicle.vin().value()));
    }

    // ===== PrepareForHandover: UC-INW-05 (preparation for handover after settlement) =====

    @Override
    public void prepareVehicleForHandover(String orderId) {
        InventoryVehicle vehicle = this.vehicleRepository.findByOrderId(new OrderId(orderId))
                .orElseThrow(() -> new VehicleNotFoundException(
                        "No reserved vehicle for order " + orderId));
        vehicle.prepareForHandover();                        // RESERVED -> READY_FOR_HANDOVER
        this.vehicleRepository.save(vehicle);
        this.eventPublisher.publish(
                new VehicleReadyForHandoverEvent(orderId, vehicle.vin().value()));
    }

    private boolean matchesSpecification(InventoryVehicle vehicle, String specificationId) {
        if (specificationId == null || vehicle.specification() == null) {
            return false;
        }
        return specificationId.equals(vehicle.specification().value());
    }
}
