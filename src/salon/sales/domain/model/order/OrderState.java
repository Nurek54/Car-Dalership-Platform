package salon.sales.domain.model.order;

public enum OrderState {
    DRAFT_CREATED,         // utworzone z oferty
    PENDING_PAYMENT,       // po podpisie, czeka na zadatek
    IN_PROGRESS,           // zadatek zaksięgowany -> realizacja (aktywne)
    CANCELLATION_STARTED,  // rozpoczęto anulowanie
    CANCELLED,             // anulowane
    COMPLETED              // zakończone (pojazd wydany)
}
