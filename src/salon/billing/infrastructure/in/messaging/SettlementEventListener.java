package salon.billing.infrastructure.in.messaging;

import salon.billing.application.service.PaymentProcessService;
import salon.common.model.Money;
import salon.common.model.OrderId;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter sterujący (driving) — subskrybent zdarzeń inicjujących rozliczenie.
 *
 * PDF rozdz. 3.7.3 ("Zarządzanie Wpłatami — Inicjalizacja"): złożenie nowego zamówienia
 * wyzwala port wejściowy, który poprzez PaymentProcessService oddelegowuje zadanie do
 * SettlementFactory — fabryka powołuje agregat salda na podstawie zamówienia uzyskanego
 * w zdarzeniu (OrderReadyForSettlement niesie orderId i wartość kontraktu).
 *
 * Idempotencja: duplikaty (ten sam eventId) są ignorowane po stronie subskrybenta.
 */
public class SettlementEventListener {

    private final PaymentProcessService settlementAppService;
    private final Set<UUID> processedEventIds = ConcurrentHashMap.newKeySet();

    public SettlementEventListener(PaymentProcessService settlementAppService) {
        if (settlementAppService == null) {
            throw new IllegalArgumentException("settlementAppService must not be null.");
        }
        this.settlementAppService = settlementAppService;
    }

    /** Nowe zamówienie gotowe do rozliczenia -> inicjalizacja agregatu salda. */
    public void on(OrderReadyForSettlementEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        if (isDuplicate(event.eventId())) {
            return;
        }
        OrderId orderId = new OrderId(event.orderId());
        Money contractValue = new Money(event.contractValue(), event.currency());
        this.settlementAppService.initializeSettlement(orderId, contractValue);
    }

    private boolean isDuplicate(UUID eventId) {
        boolean firstTime = this.processedEventIds.add(eventId);
        if (!firstTime) {
            System.out.println("[SettlementEventListener] Duplicate event ignored: " + eventId);
        }
        return !firstTime;
    }
}
