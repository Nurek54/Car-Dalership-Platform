package salon.sales.application.port.out;

/**
 * OUTBOUND PORT (Figure 22) — "InventoryIntegration".
 * UC-CRM-05: sends the ReleaseVehicle command to the Inventory and Logistics Context once the
 * physical handover is confirmed. The adapter (InventoryExternalAPI) handles the technical call.
 */
public interface InventoryIntegration {

    void releaseVehicle(String orderId);
}
