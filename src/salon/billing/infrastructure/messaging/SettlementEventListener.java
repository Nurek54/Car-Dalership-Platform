package salon.billing.infrastructure.messaging;

import salon.billing.application.port.in.CalculateSettlementCommand;
import salon.billing.application.port.in.CalculateSettlementUseCase;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter WEJŚCIOWY (driving) sterowany zdarzeniem — odpala UC-ROZ-03.
 *
 * Broker dostarcza zdarzenie, listener mapuje je na komendę i woła port wejściowy.
 * Domena/aplikacja nie wiedzą, że trigger przyszedł z kolejki (zależność do wewnątrz, 3.1).
 * Idempotencyjność (3.4.2): to Subskrybent pilnuje duplikatów po eventId.
 */
public class SettlementEventListener {

    private final CalculateSettlementUseCase calculateSettlement;
    private final Set<UUID> processedEventIds = ConcurrentHashMap.newKeySet();

    public SettlementEventListener(CalculateSettlementUseCase calculateSettlement) {
        if (calculateSettlement == null) {
            throw new IllegalArgumentException("calculateSettlement must not be null.");
        }
        this.calculateSettlement = calculateSettlement;
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
        CalculateSettlementCommand command = new CalculateSettlementCommand(
                event.orderId(),
                event.vehicleValue(),
                event.totalDeposits(),
                event.currency());
        this.calculateSettlement.calculateSettlement(command);
    }
}
