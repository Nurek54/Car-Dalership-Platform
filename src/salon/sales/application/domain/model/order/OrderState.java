package salon.sales.application.domain.model.order;

/**
 * Cykl życia zamówienia — zgodnie z docs/Agregate/Sales/customer-offer-order.md
 * i docs/Agregate/Sales/order.md oraz diagramami UC-CRM-03/04/05 z PDF.
 *
 * DRAFT_CREATED -> (DRAFT po formalnym złożeniu przez fabrykę) -> IN_PROGRESS
 * -> READY_FOR_HANDOVER -> HANDOVER_SCHEDULED -> COMPLETED; CANCELLED to stan boczny.
 */
public enum OrderState {
    DRAFT_CREATED,         // świeżo utworzone z oferty (konstruktor agregatu)
    DRAFT,                 // formalnie złożone przez OrderFactory (OrderPlacedEvent)
    IN_PROGRESS,           // "W realizacji" — wpłata zaksięgowana / realizacja trwa
    READY_FOR_HANDOVER,    // "Gotowe do odbioru" — pojazd gotowy fizycznie i finansowo (UC-CRM-04)
    HANDOVER_SCHEDULED,    // "Umówiony na odbiór" — ustalony termin odbioru (UC-CRM-04)
    CANCELLED,             // anulowane (klient zrezygnował)
    COMPLETED              // "Zrealizowane" — pojazd wydany (UC-CRM-05)
}
