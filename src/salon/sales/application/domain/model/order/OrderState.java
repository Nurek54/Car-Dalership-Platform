package salon.sales.application.domain.model.order;

/**
 * The order's life cycle — per docs/Agregate/Sales/customer-offer-order.md
 * and docs/Agregate/Sales/order.md and the UC-CRM-03/04/05 diagrams from the PDF.
 *
 * DRAFT_CREATED -> (DRAFT after formal placement by the factory) -> IN_PROGRESS
 * -> READY_FOR_HANDOVER -> HANDOVER_SCHEDULED -> COMPLETED; CANCELLED is a side state.
 */
public enum OrderState {
    DRAFT_CREATED,         // freshly created from the offer (aggregate constructor)
    DRAFT,                 // formally placed by OrderFactory (OrderPlacedEvent)
    IN_PROGRESS,           // "In progress" — payment posted / fulfillment ongoing
    READY_FOR_HANDOVER,    // "Ready for handover" — vehicle ready physically and financially (UC-CRM-04)
    HANDOVER_SCHEDULED,    // "Handover scheduled" — the agreed pickup date (UC-CRM-04)
    CANCELLED,             // cancelled (the customer withdrew)
    COMPLETED              // "Completed" — vehicle handed over (UC-CRM-05)
}
