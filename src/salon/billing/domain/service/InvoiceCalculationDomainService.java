package salon.billing.domain.service;

import salon.billing.domain.model.settlement.Settlement;
import salon.shared.model.Money;

import java.math.BigDecimal;

/**
 * Bezstanowy serwis domenowy chroniący warstwę aplikacji przed wyciekiem reguł księgowych.
 *
 * W sposób kontrolowany sięga do stanu agregatu Settlement (uses state of) i wylicza kwoty:
 *  - faktury końcowej = wartość kontraktu - zaksięgowane wpłaty (saldo pozostałe),
 *  - zadatku = ustalony procent wartości kontraktu.
 * Zwraca czysty obiekt wartości Money — co drastycznie zwiększa testowalność matematyki.
 */
public class InvoiceCalculationDomainService {

    // Przykładowa reguła: zadatek = 10% wartości kontraktu.
    private static final BigDecimal ADVANCE_RATE = new BigDecimal("0.10");

    public Money calculateFinalInvoiceAmount(Settlement settlement) {
        if (settlement == null) {
            throw new IllegalArgumentException("settlement must not be null.");
        }
        return settlement.getOutstandingBalance();
    }

    public Money calculateAdvanceAmount(Settlement settlement) {
        if (settlement == null) {
            throw new IllegalArgumentException("settlement must not be null.");
        }
        Money total = settlement.getTotalAmount();
        return new Money(total.amount().multiply(ADVANCE_RATE), total.currency());
    }
}
