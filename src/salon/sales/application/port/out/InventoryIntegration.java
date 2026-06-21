package salon.sales.application.port.out;

/**
 * Outbound port (driven) to the Inventory and Logistics Context —
 * the "InventoryIntegration" node in docs/Architecture/SalesArchitecture.md
 * (PDF chapter 3.3.3: "reservations, releasing locks in the yard").
 */
public interface InventoryIntegration {

    /** UC-CRM-03/UC-INW-01..02: allocation of a vehicle from the yard or a production slot for the order. */
    void allocateVehicleOrProductionSlot(String orderId);

    /** UC-CRM-05, step 3: the ReleaseVehicle command — removing the physical car from stock (UC-INW-06). */
    void releasePhysicalVehicle(String vehicleId);
}
