package salon.sales.application.port.in;

/**
 * INBOUND PORT (Figure 22) — "ReleaseVehicle".
 * UC-CRM-05: register the physical handover — complete the order and send the ReleaseVehicle
 * command to the Inventory Context.
 */
public interface ReleaseVehicle {

    void releaseVehicle(String orderId);
}
