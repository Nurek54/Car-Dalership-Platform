package salon.sales.infrastructure.in.messaging;

import salon.common.infrastructure.messaging.RabbitMqMessageHandler;
import salon.sales.application.port.in.ActivateOrderOnDeposit;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * INBOUND ADAPTER (Figure 22: EventListener over the broker) — subscriber of the Billing Context's
 * PaymentRegisteredEvent delivered asynchronously via RabbitMQ (UC-CRM-03, part 2).
 *
 * Idempotency (section 3.4.2) is handled here, in the subscriber: an already-seen eventId is ignored.
 * The adapter translates the flat message into a call on the {@link ActivateOrderOnDeposit} port.
 */
public class SalesDepositListener implements RabbitMqMessageHandler {

    private final ActivateOrderOnDeposit activateOrderOnDeposit;
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    public SalesDepositListener(ActivateOrderOnDeposit activateOrderOnDeposit) {
        if (activateOrderOnDeposit == null) {
            throw new IllegalArgumentException("activateOrderOnDeposit must not be null.");
        }
        this.activateOrderOnDeposit = activateOrderOnDeposit;
    }

    @Override
    public void handle(Map<String, String> event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        String eventId = event.get("eventId");
        if (eventId != null && !this.processedEventIds.add(eventId)) {
            return; // already processed — idempotent.
        }
        String orderId = event.get("orderId");
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        this.activateOrderOnDeposit.activateOnDeposit(orderId);
    }
}
