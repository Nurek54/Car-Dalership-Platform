package salon.billing.application.domain.service;

import salon.billing.application.domain.model.settlement.Settlement;
import salon.common.model.Money;

import java.math.BigDecimal;

/**
 * DOMAIN SERVICE (Fig. 48 — InvoiceCalculationService).
 *
 * Stateless, performs business calculations based on the Settlement aggregate that are not performed by
 * the aggregate itself: the deposit amount (UC-FIR-01) and the amount remaining due on the final invoice
 * (UC-FIR-02). Returns a Money value object without exposing the aggregate internals to clients.
 */
public class InvoiceCalculationService {

    /** Default deposit rate = 10% of the contract value (UC-FIR-01). */
    private static final BigDecimal ADVANCE_RATE = new BigDecimal("0.10");

    /** UC-FIR-01, step 2: deposit amount = 10% of the order value. */
    public Money calculateAdvanceAmount(Settlement settlement) {
        if (settlement == null) {
            throw new IllegalArgumentException("settlement must not be null.");
        }
        Money total = settlement.totalAmount();
        return Money.of(total.getAmount().multiply(ADVANCE_RATE), total.currency());
    }

    /** UC-FIR-02, step 2: final invoice amount = remaining balance (after accounting for the deposit). */
    public Money calculateFinalInvoiceAmount(Settlement settlement) {
        if (settlement == null) {
            throw new IllegalArgumentException("settlement must not be null.");
        }
        return settlement.outstandingBalance();
    }
}
