package salon.billing.infrastructure.messaging;

import salon.billing.application.service.SettlementAppService;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter WEJŚCIOWY (driving) sterowany zdarzeniem — inicjalizuje rozliczenie (UC-FIR-03).
 *
 * Broker dostarcza zdarzenie o złożeniu zamówienia, listener mapuje je i woła usługę aplikacji,
 * która przez SettlementFactory powołuje agregat salda. Domena/aplikacja nie wiedzą, że trigger
 * przyszedł z kolejki (zależność do wewnątrz). Idempotencyjność: Subskrybent pilnuje duplikatów.
 */
public class SettlementEventListener {

    private final SettlementAppService settlementAppService;
    private final Set<UUID> processedEventIds = ConcurrentHashMap.newKeySet();

    public SettlementEventListener(SettlementAppService settlementAppService) {
        if (settlementAppService == null) {
            throw new IllegalArgumentException("settlementAppService must not be null.");
        }
        this.settlementAppService = settlementAppService;
    }

    public void on(OrderReadyForSettlementEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        boolean firstTime = this.processedEventIds.add(event.eventId());
        if (!firstTime) {
            System.out.println("[SettlementEventListener] Duplicate event ignored: " + event.eventId());
            return;
        }
        OrderId orderId = new OrderId(event.orderId());
        Money contractValue = new Money(event.contractValue(), event.currency());
        this.settlementAppService.initializeSettlement(orderId, contractValue);
    }
}
