package salon.sales.application.domain.model.order;

/**
 * Enumeration (Figure 23) — lifecycle state of an Order.
 *
 * DRAFT_CREATED – created from an accepted offer (not yet active);
 * IN_PROGRESS – activated once the deposit/financing path started;
 * READY_FOR_HANDOVER – vehicle ready (UC-CRM-04); HANDOVER_SCHEDULED – pickup date agreed;
 * COMPLETED – vehicle physically handed over (UC-CRM-05); CANCELLED – the customer withdrew.
 */
public enum OrderState {
    DRAFT_CREATED,
    IN_PROGRESS,
    READY_FOR_HANDOVER,
    HANDOVER_SCHEDULED,
    COMPLETED,
    CANCELLED
}
