package salon.logistics.application.domain.model.vehicle;

/**
 * The life-cycle state of a vehicle instance in Inventory (structural model, Figure 38).
 *
 * ON_STOCK ("Free") – available in the yard, without a reservation;
 * IN_PRODUCTION ("In production") – ordered from the factory, not yet in the yard (UC-INW-02);
 * RESERVED ("Reserved") – a hard lock on the VIN for a specific order;
 * READY_FOR_HANDOVER ("Ready for handover") – balance settled, awaiting pickup (UC-INW-05);
 * HANDED_OVER ("Handed over") – removed from the active stock (UC-INW-06).
 *
 * READY_FOR_HANDOVER follows directly from the use cases UC-INW-05/06 (PDF); the class diagram
 * presents the states in a simplified version.
 */
public enum VehicleState {
    ON_STOCK,
    IN_PRODUCTION,
    RESERVED,
    READY_FOR_HANDOVER,
    HANDED_OVER
}
