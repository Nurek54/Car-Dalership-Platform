package salon.logistics.domain.model.vehicle;

/**
 * Stan logistyczny pojazdu — zgodnie z diagramem agregatu (inventory-vehicle.md).
 *
 * Cykl: IN_PRODUCTION -> ON_STOCK -> (RESERVED) -> HANDED_OVER.
 */
public enum VehicleState {
    ON_STOCK,       // na placu salonu (w stanie magazynowym)
    IN_PRODUCTION,  // w produkcji u importera/producenta
    RESERVED,       // zablokowany pod konkretne zamówienie
    HANDED_OVER     // wydany klientowi
}
