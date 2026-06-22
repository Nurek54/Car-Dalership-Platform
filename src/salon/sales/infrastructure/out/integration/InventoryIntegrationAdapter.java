package salon.sales.infrastructure.out.integration;

import salon.sales.application.port.out.InventoryIntegration;

/**
 * OUTBOUND ADAPTER (ACL, Figure 22) — integration with the Inventory and Logistics Context.
 *
 * Sends the "release vehicle" command after a physical handover (UC-CRM-05); here simulated by
 * logging. In a distributed setup this maps onto a ReleaseVehicle command message consumed by
 * the Inventory Context.
 */
public class InventoryIntegrationAdapter implements InventoryIntegration {

    @Override
    public void releaseVehicle(String orderId) {
        System.out.println("[InventoryIntegrationAdapter] Release-vehicle command sent to Inventory"
                + " for order " + orderId + ".");
    }
}
