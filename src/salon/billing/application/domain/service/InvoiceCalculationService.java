package salon.billing.application.domain.service;

import salon.billing.application.domain.model.settlement.Settlement;
import salon.common.model.Money;

import java.math.BigDecimal;

public class InvoiceCalculationService {

    
    private static final BigDecimal ADVANCE_RATE = new BigDecimal("0.10");

    
    public Money calculateAdvanceAmount(Settlement settlement) {
        if (settlement == null) {
            throw new IllegalArgumentException("settlement must not be null.");
        }
        Money total = settlement.totalAmount();
        return Money.of(total.getAmount().multiply(ADVANCE_RATE), total.currency());
    }

    
    public Money calculateFinalInvoiceAmount(Settlement settlement) {
        if (settlement == null) {
            throw new IllegalArgumentException("settlement must not be null.");
        }
        return settlement.outstandingBalance();
    }
}
