package main.java.com.salon.billing.domain.service;

import main.java.com.salon.billing.domain.model.settlement.OrderSettlement;

/**
 * Serwis dziedzinowy (LOGIKA BIZNESOWA): porządkuje kroki obliczenia salda na agregacie.
 * Dzięki temu serwis aplikacyjny pozostaje cienki — woła jedną metodę.
 */
public class SettlementCalculationService {

    public void process(OrderSettlement settlement) {
        if (settlement == null) {
            throw new IllegalArgumentException("settlement must not be null.");
        }
        settlement.calculateBalance();    // krok 6
        settlement.checkForOverpayment(); // A1
    }
}
