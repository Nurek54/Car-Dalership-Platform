package salon.billing.application.domain.model.settlement;

/**
 * Standard type (Enumeration in the class diagram) — state of the order balance.
 *
 * OPEN            – balance open, no (full) payments;
 * PARTIAL_PAYMENT – a partial payment was posted (UC-FIR-03 / A1);
 * SETTLED         – balance = 0, order fully paid (UC-FIR-03).
 */
public enum SettlementStatus {
    OPEN,
    PARTIAL_PAYMENT,
    SETTLED
}
