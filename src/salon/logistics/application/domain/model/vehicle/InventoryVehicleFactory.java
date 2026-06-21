package salon.logistics.application.domain.model.vehicle;

import salon.common.model.OrderId;

/**
 * Factory of the {@link InventoryVehicle} aggregate — the only way to create instances
 * (the aggregate's constructor is package-private). It enforces a correct initial state
 * depending on the path by which the vehicle is created in Inventory.
 */
public class InventoryVehicleFactory {

    /** UC-INW-02: a vehicle ordered from the factory — created already assigned to the order. */
    public InventoryVehicle createForFactoryOrder(VinNumber vin, OrderId order, SpecificationId specification) {
        if (order == null) {
            throw new IllegalArgumentException("order must not be null.");
        }
        return new InventoryVehicle(vin, specification, VehicleRole.STOCK, VehicleState.IN_PRODUCTION, order);
    }

    /** UC-INW-03 / A1: a physical vehicle received into the yard without an order — available as free. */
    public InventoryVehicle createStockArrival(ImporterData data) {
        if (data == null) {
            throw new IllegalArgumentException("data must not be null.");
        }
        return new InventoryVehicle(data.vin(), data.specificationId(),
                VehicleRole.STOCK, VehicleState.ON_STOCK, null);
    }
}
