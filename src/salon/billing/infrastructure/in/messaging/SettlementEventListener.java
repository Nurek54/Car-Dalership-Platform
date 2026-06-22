package salon.billing.infrastructure.in.messaging;

import salon.billing.application.port.in.ProcessPayment;
import salon.common.model.Money;
import salon.common.model.OrderId;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
            return;
        }
        this.processPayment.initializeSettlement(
                new OrderId(event.orderId()),
                Money.of(event.totalAmount(), event.currency()));
    }
}
