package salon.sales.infrastructure.out.integration;

import salon.sales.application.port.out.InventoryIntegration;

public class InventoryIntegrationAdapter implements InventoryIntegration {

    @Override
    public void releaseVehicle(String orderId) {
        System.out.println("[InventoryIntegrationAdapter] Release-vehicle command sent to Inventory"
                + " for order " + orderId + ".");
    }
}
