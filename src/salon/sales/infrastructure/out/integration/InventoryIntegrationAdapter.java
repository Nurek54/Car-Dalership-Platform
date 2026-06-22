package salon.sales.infrastructure.out.integration;

import salon.sales.application.port.out.InventoryIntegration;

public class InventoryIntegrationAdapter implements InventoryIntegration {

    @Override
    public void releaseVehicle(String orderId) {
        System.out.println("[InventoryIntegrationAdapter] Release-vehicle command sent to Inventory"
                + " for order " + orderId + ".");
    }

    @Override
    public void releasePhysicalVehicle(String orderId) {
        System.out.println("[InventoryIntegrationAdapter] Physical vehicle released for order " + orderId + ".");
    }

    @Override
    public void allocateVehicleOrProductionSlot(String orderId) {
        System.out.println("[InventoryIntegrationAdapter] Vehicle/production slot allocated for order "
                + orderId + ".");
    }
}
