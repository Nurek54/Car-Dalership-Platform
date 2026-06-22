package salon.sales.application.port.out;

/**
 * OUTBOUND PORT (Figure 22) — "InventoryIntegration" to the Inventory and Logistics Context.
 * Covers allocating a vehicle/production slot when an order is placed and releasing the physical
 * vehicle on handover (UC-CRM-05).
 */
public interface InventoryIntegration {

    /** UC-CRM-05: releases the physical vehicle once the handover is confirmed. */
    void releaseVehicle(String orderId);

    /** UC-CRM-05: removes the physical vehicle from the warehouse/yard for the given order. */
    void releasePhysicalVehicle(String orderId);

    /** UC-CRM-03: allocates a vehicle from stock or a production slot for the placed order. */
    void allocateVehicleOrProductionSlot(String orderId);
}
