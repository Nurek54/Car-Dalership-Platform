package salon.billing.domain.service;

import salon.shared.model.Money;

import java.math.BigDecimal;

/**
 * Serwis dziedzinowy: reguła "ile wynosi wymagany zadatek?" (UC-ROZ-01).
 * Reguła nie należy do pojedynczej wpłaty, więc trzymamy ją osobno.
 */
public class PaymentClassificationService {

    // PRZYKŁADOWA reguła: zadatek = 10% wartości zamówienia.
    private static final BigDecimal DEPOSIT_RATE = new BigDecimal("0.10");

    public Money calculateRequiredDeposit(Money orderValue) {
        if (orderValue == null) {
            throw new IllegalArgumentException("orderValue must not be null.");
        }
        BigDecimal requiredAmount = orderValue.amount().multiply(DEPOSIT_RATE);
        return new Money(requiredAmount, orderValue.currency());
    }
}
