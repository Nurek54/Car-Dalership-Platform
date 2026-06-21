package salon.sales.application.port.in;

import salon.common.model.OrderId;

/**
 * Inbound port for UC-CRM-05 (registering the physical vehicle handover) —
 * the "ReleaseVehicle" node in docs/Architecture/SalesArchitecture.md (PDF chapter 3.3.3).
 *
 * Closes the transaction (order -> "Completed"), sends the ReleaseVehicle command
 * to the Inventory Context and instructs Billing to close the balance.
 */
public interface ReleaseVehicle {

    void confirmHandover(OrderId orderId);
}
