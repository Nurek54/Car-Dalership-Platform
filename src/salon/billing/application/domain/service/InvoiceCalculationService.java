package salon.billing.application.domain.service;

import salon.billing.application.domain.model.settlement.Settlement;
import salon.common.model.Money;

import java.math.BigDecimal;

/**
 * USLUGA DZIEDZINY (Rys. 48 — InvoiceCalculationService).
 *
 * Bezstanowa, realizuje obliczenia biznesowe na podstawie agregatu Rozliczenia, ktorych nie wykonuje
 * sam agregat: kwote zadatku (UC-FIR-01) i kwote pozostala do zaplaty na fakturze koncowej
 * (UC-FIR-02). Zwraca obiekt wartosci Money, nie ujawniajac wnetrza agregatu klientom.
 */
public class InvoiceCalculationService {

    /** Domyslna stawka zadatku = 10% wartosci kontraktu (UC-FIR-01). */
    private static final BigDecimal ADVANCE_RATE = new BigDecimal("0.10");

    /** UC-FIR-01, krok 2: kwota zadatku = 10% wartosci zamowienia. */
    public Money calculateAdvanceAmount(Settlement settlement) {
        if (settlement == null) {
            throw new IllegalArgumentException("settlement must not be null.");
        }
        Money total = settlement.totalAmount();
        return Money.of(total.getAmount().multiply(ADVANCE_RATE), total.currency());
    }

    /** UC-FIR-02, krok 2: kwota faktury koncowej = saldo pozostale (po uwzglednieniu zadatku). */
    public Money calculateFinalInvoiceAmount(Settlement settlement) {
        if (settlement == null) {
            throw new IllegalArgumentException("settlement must not be null.");
        }
        return settlement.outstandingBalance();
    }
}
