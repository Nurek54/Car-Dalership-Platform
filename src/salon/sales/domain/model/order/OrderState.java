package salon.sales.domain.model.order;

public enum OrderState {
    DRAFT_CREATED,         // utworzone z oferty
    PENDING_PAYMENT,       // po podpisie, czeka na zadatek
    IN_PROGRESS,           // zadatek zaksięgowany -> realizacja (aktywne)
    READY_FOR_HANDOVER,    // pojazd gotowy fizycznie i finansowo (UC-CRM-04)
    HANDOVER_SCHEDULED,    // umówiony termin odbioru — "Umówiony na odbiór" (UC-CRM-04)
    CANCELLATION_STARTED,  // rozpoczęto anulowanie
    CANCELLED,             // anulowane
    COMPLETED              // zakończone (pojazd wydany)
}
