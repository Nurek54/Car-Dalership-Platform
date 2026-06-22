package salon.logistics.application.domain.model.vehicle;

import salon.common.model.OrderId;

public class InventoryVehicleFactory {

    public InventoryVehicle createForFactoryOrder(VinNumber vin, OrderId order, SpecificationId specification) {
        if (order == null) {
            throw new IllegalArgumentException("order must not be null.");
        }
        return new InventoryVehicle(vin, specification, VehicleRole.STOCK, VehicleState.IN_PRODUCTION, order);
    }

    public InventoryVehicle createStockArrival(ImporterData data) {
        if (data == null) {
            throw new IllegalArgumentException("data must not be null.");
        }
        return new InventoryVehicle(data.vin(), data.specificationId(),
                VehicleRole.STOCK, VehicleState.ON_STOCK, null);
    }
}
