package salon.financing.domain.model.financing;

/**
 * Stan wniosku finansowego — zgodnie z diagramem agregatu (financing-application.md).
 */
public enum ApplicationState {
    DRAFT,
    PENDING,
    APPROVED,
    REJECTED
}
