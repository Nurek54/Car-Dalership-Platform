package salon.billing.infrastructure.in.messaging;

import salon.billing.application.port.in.ProcessPayment;
import salon.common.model.Money;
import salon.common.model.OrderId;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ADAPTER WEJSCIOWY (Rys. 48 — EventListener) — subskrybent inicjalizacji salda.
 *
 * Mapuje {@link OrderReadyForSettlementEvent} z magistrali na port wejsciowy {@link ProcessPayment}
 * (initializeSettlement). Realizuje DEDUPLIKACJE (sekcja 3.4.2) — to odpowiedzialnosc subskrybenta,
 * nie konsumenta kolejki: powtorzone zdarzenie o tym samym eventId jest przetwarzane tylko raz.
 */
public class SettlementEventListener {

    private final ProcessPayment processPayment;
    private final Set<UUID> processedEventIds = ConcurrentHashMap.newKeySet();

    public SettlementEventListener(ProcessPayment processPayment) {
        if (processPayment == null) {
            throw new IllegalArgumentException("processPayment must not be null.");
        }
        this.processPayment = processPayment;
    }

    public void on(OrderReadyForSettlementEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        if (!this.processedEventIds.add(event.eventId())) {
            return; // duplikat — pomijamy (idempotencyjnosc)
        }
        this.processPayment.initializeSettlement(
                new OrderId(event.orderId()),
                Money.of(event.totalAmount(), event.currency()));
    }
}
