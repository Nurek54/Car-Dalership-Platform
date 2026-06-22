package salon.sales.application.domain.model.order;

/**
 * Enumeration (Figure 23) — lifecycle state of an Order.
 *
 * IN_PROGRESS – created from an accepted offer; READY_FOR_HANDOVER – vehicle ready (UC-CRM-04);
 * HANDOVER_SCHEDULED – pickup date agreed; COMPLETED – vehicle physically handed over (UC-CRM-05).
 */
public enum OrderState {
    IN_PROGRESS,
    READY_FOR_HANDOVER,
    HANDOVER_SCHEDULED,
    COMPLETED
}
