package salon.billing.infrastructure.in.messaging;

import salon.billing.application.port.in.ProcessPayment;
import salon.common.model.Money;
import salon.common.model.OrderId;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * INBOUND ADAPTER (Fig. 48 — EventListener) — subscriber for balance initialization.
 *
 * Maps {@link OrderReadyForSettlementEvent} from the bus to the inbound port {@link ProcessPayment}
 * (initializeSettlement). It performs DEDUPLICATION (section 3.4.2) — this is the subscriber's responsibility,
 * not the queue consumer: a repeated event with the same eventId is processed only once.
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
