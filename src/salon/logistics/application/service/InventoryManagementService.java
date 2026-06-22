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

    public void registerSpecification(String specificationId, List<String> optionCodes) {
        this.catalogIntegration.saveSpecification(specificationId, optionCodes);
    }

    public void linkOrderToSpecification(String orderId, String specificationId) {
        this.catalogIntegration.linkOrderToSpecification(orderId, specificationId);
    }

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
            vehicle.lockForOrder(order);
            this.vehicleRepository.save(vehicle);
            this.eventPublisher.publish(
                    new VehicleReservedFromStockEvent(orderId, vehicle.vin().value()));
        } else {
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

    @Override
    public void receiveVehicle(String vin) {
        VinNumber vinNumber = new VinNumber(vin);
        Optional<InventoryVehicle> existing = this.vehicleRepository.findByVin(vinNumber);

        if (existing.isPresent()) {
            InventoryVehicle vehicle = existing.get();
            ImporterData data = new ImporterData(vinNumber, vehicle.specification(), List.of());
            vehicle.receiveOnYard(data);
            this.vehicleRepository.save(vehicle);
            OrderId order = vehicle.order();
            this.eventPublisher.publish(new VehicleDeliveredToStockEvent(
                    order == null ? null : order.value(), vin));
        } else {
            ImporterData data = new ImporterData(vinNumber, null, List.of());
            this.vehicleRepository.save(this.vehicleFactory.createStockArrival(data));
        }
    }

    @Override
    public void releaseVehicle(String orderId) {
        InventoryVehicle vehicle = this.vehicleRepository.findByOrderId(new OrderId(orderId))
                .orElseThrow(() -> new VehicleNotFoundException(
                        "No vehicle to hand over for order " + orderId));
        try {
            vehicle.handOver();
            this.vehicleRepository.save(vehicle);
            this.eventPublisher.publish(
                    new VehicleInventoryReleasedEvent(orderId, vehicle.vin().value()));
        } catch (IllegalVehicleStateException e) {
            this.eventPublisher.publish(new VehicleInventoryReleasedErrorEvent(orderId, e.getMessage()));
        }
    }

    @Override
    public void releaseReservation(String orderId) {
        Optional<InventoryVehicle> reserved = this.vehicleRepository.findByOrderId(new OrderId(orderId));
        if (reserved.isEmpty()) {
            return;
        }
        InventoryVehicle vehicle = reserved.get();
        vehicle.releaseReservation();
        this.vehicleRepository.save(vehicle);
        this.eventPublisher.publish(
                new VehicleReservationCancelledEvent(orderId, vehicle.vin().value()));
    }

    @Override
    public void prepareVehicleForHandover(String orderId) {
        InventoryVehicle vehicle = this.vehicleRepository.findByOrderId(new OrderId(orderId))
                .orElseThrow(() -> new VehicleNotFoundException(
                        "No reserved vehicle for order " + orderId));
        vehicle.prepareForHandover();
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
