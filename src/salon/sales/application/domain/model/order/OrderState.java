package salon.sales.application.domain.model.order;

public enum OrderState {
    DRAFT_CREATED,
    IN_PROGRESS,
    READY_FOR_HANDOVER,
    HANDOVER_SCHEDULED,
    COMPLETED,
    CANCELLED
}
