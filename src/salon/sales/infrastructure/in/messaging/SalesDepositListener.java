package salon.sales.infrastructure.in.messaging;

import salon.sales.application.port.in.ActivateOrderOnDeposit;
import salon.common.infrastructure.messaging.RabbitMqMessageHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * INBOUND adapter (driving) of the Sales context: it listens for the "PaymentRegisteredEvent" event
 * (DepositPosted) coming FROM BILLING THROUGH THE RabbitMQ QUEUE and triggers the activation
 * of the order (UC-SPR-02, step 5).
 *
 * Idempotency (3.4.2): it is HERE — on the subscriber side — that we guard against duplicates by eventId.
 * Rejestrujemy ten handler w RabbitMqEventConsumer pod kluczem "PaymentRegisteredEvent"
 * (a posted payment/deposit from the Billing Context — UC-CRM-03 part 2, Fig. 19/20 PDF).
 */
public class SalesDepositListener implements RabbitMqMessageHandler {

    private final ActivateOrderOnDeposit activateOrder;
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    public SalesDepositListener(ActivateOrderOnDeposit activateOrder) {
        if (activateOrder == null) {
            throw new IllegalArgumentException("activateOrder must not be null.");
        }
        this.activateOrder = activateOrder;
    }

    @Override
    public void handle(Map<String, String> event) {
        if (event == null) {
            return;
        }
        String eventId = event.get("eventId");
        boolean firstTime = this.processedEventIds.add(eventId);
        if (!firstTime) {
            System.out.println("[SalesDepositListener] Duplicate event ignored: " + eventId);
            return;
        }
        String orderId = event.get("orderId");
        System.out.println("[SalesDepositListener] Deposit booked for order " + orderId
                + " -> activating order.");
        this.activateOrder.activateOnDeposit(orderId);
    }
}
