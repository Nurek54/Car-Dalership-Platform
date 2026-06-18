package salon.billing.application.domain.model.settlement;

import salon.common.model.Money;
import salon.common.model.OrderId;

/**
 * Fabryka agregatu Settlement (UC-FIR-03, inicjalizacja).
 *
 * Powołuje do życia agregat salda na podstawie zamówienia uzyskanego ze zdarzenia.
 * Trzyma logikę poprawnej konstrukcji z dala od usługi aplikacyjnej.
 */
public class SettlementFactory {

    public Settlement createForOrder(OrderId orderId, Money contractValue) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        if (contractValue == null) {
            throw new IllegalArgumentException("contractValue must not be null.");
        }
        return new Settlement(SettlementId.generate(), orderId, contractValue);
    }
}
