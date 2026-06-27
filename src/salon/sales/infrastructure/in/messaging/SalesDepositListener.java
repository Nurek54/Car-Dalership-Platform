package salon.sales.infrastructure.in.messaging;

import salon.common.infrastructure.messaging.RabbitMqMessageHandler;
import salon.sales.application.port.in.ActivateOrderOnDeposit;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
            return; 
        }
        String orderId = event.get("orderId");
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        this.activateOrderOnDeposit.activateOnDeposit(orderId);
    }
}
