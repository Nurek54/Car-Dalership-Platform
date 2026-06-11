package salon.sales.infrastructure.messaging;

import salon.sales.application.port.in.ActivateOrderOnDepositUseCase;
import salon.shared.infrastructure.messaging.RabbitMqMessageHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter WEJŚCIOWY (driving) kontekstu Sprzedaży: nasłuchuje zdarzenia "PaymentRegisteredEvent"
 * (ZadatekZaksiegowany) przychodzącego Z ROZLICZEŃ PRZEZ KOLEJKĘ RabbitMQ i odpala aktywację
 * zamówienia (UC-SPR-02, krok 5).
 *
 * Idempotencyjność (3.4.2): to TUTAJ — po stronie subskrybenta — pilnujemy duplikatów po eventId.
 * Rejestrujemy ten handler w RabbitMqEventConsumer pod kluczem "PaymentRegisteredEvent"
 * (zaksięgowana wpłata/zadatek z Kontekstu Rozliczeń — UC-CRM-03 cz.2, Rys. 19/20 PDF).
 */
public class SalesDepositListener implements RabbitMqMessageHandler {

    private final ActivateOrderOnDepositUseCase activateOrder;
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    public SalesDepositListener(ActivateOrderOnDepositUseCase activateOrder) {
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
