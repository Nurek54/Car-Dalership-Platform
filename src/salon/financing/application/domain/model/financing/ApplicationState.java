package salon.financing.application.domain.model.financing;

/**
 * State of the financing application (Figure 43).
 *
 * DRAFT – the application created locally from the order data;
 * PENDING – sent to the bank, "Under bank verification" (UC-FIN-01);
 * APPROVED – the bank issued a positive decision (UC-FIN-02);
 * REJECTED – the bank issued a negative decision (UC-FIN-02 / A1).
 */
public enum ApplicationState {
    DRAFT,
    PENDING,
    APPROVED,
    REJECTED
}
