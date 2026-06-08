package salon.logistics.domain.model.slot;

public enum SlotState {
    SCHEDULED,      // zamówiony u producenta
    IN_PRODUCTION,  // w produkcji
    IN_TRANSIT,     // w transporcie
    CANCELLED       // anulowany (UC-INW-06)
}
