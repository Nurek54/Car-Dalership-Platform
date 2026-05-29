package main.java.com.salon.billing.domain.service;

import main.java.com.salon.billing.domain.model.shared.Money;

import java.math.BigDecimal;

/**
 * Serwis dziedzinowy: trzyma regułę "ile wynosi wymagany zadatek?".
 *
 * Ta reguła nie należy do pojedynczej wpłaty (Payment), dlatego wyciągamy ją
 * do serwisu dziedzinowego. Sama wpłata wie tylko, jak się sklasyfikować
 * względem podanego progu.
 */
public class PaymentClassificationService {

    // PRZYKŁADOWA reguła: zadatek = 10% wartości zamówienia.
    // W realnym systemie byłaby konfigurowalna (np. per typ pojazdu).
    private static final BigDecimal DEPOSIT_RATE = new BigDecimal("0.10");

    public Money calculateRequiredDeposit(Money orderValue) {
        if (orderValue == null) {
            throw new IllegalArgumentException("orderValue nie może być nullem.");
        }
        BigDecimal requiredAmount = orderValue.amount().multiply(DEPOSIT_RATE);
        return new Money(requiredAmount, orderValue.currency());
    }
}