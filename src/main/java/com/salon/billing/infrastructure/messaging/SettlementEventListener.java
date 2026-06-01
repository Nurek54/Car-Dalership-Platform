package main.java.com.salon.billing.infrastructure.messaging;

import main.java.com.salon.billing.application.port.in.CalculateSettlementCommand;
import main.java.com.salon.billing.application.port.in.CalculateSettlementUseCase;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**+
 * Adapter WEJŚCIOWY (driving) sterowany zdarzeniem — odpala UC-ROZ-03.
 *
 * To tutaj "wołany" jest port wejściowy CalculateSettlementUseCase: broker dostarcza
 * zdarzenie, listener mapuje je na komendę i wywołuje use case. Domena/aplikacja
 * nie wiedzą, że trigger przyszedł z kolejki — zależność jest skierowana do wewnątrz (3.1).
 *
 * Idempotencyjność (sekcja 3.4.2): to Subskrybent pilnuje duplikatów po eventId.
 */
public class SettlementEventListener {

    private final CalculateSettlementUseCase calculateSettlement;

    // Pamięć przetworzonych zdarzeń — w realnym systemie byłaby trwała (np. tabela w bazie).
    private final Set<UUID> processedEventIds = ConcurrentHashMap.newKeySet();

    public SettlementEventListener(CalculateSettlementUseCase calculateSettlement) {
        if (calculateSettlement == null) {
            throw new IllegalArgumentException("calculateSettlement must not be null.");
        }
        this.calculateSettlement = calculateSettlement;
    }

    /**
     * Metoda wołana przez infrastrukturę komunikatów (np. @RabbitListener) po odebraniu zdarzenia.
     */
    public void on(OrderReadyForSettlementEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }

        // Deduplikacja: jeśli już przetwarzaliśmy to zdarzenie, ignorujemy duplikat (3.4.2).
        boolean firstTime = processedEventIds.add(event.eventId());
        if (!firstTime) {
            System.out.println("[SettlementEventListener] Duplicate event ignored: " + event.eventId());
            return;
        }

        // Mapowanie zdarzenia -> komenda i wywołanie portu wejściowego (UC-ROZ-03).
        CalculateSettlementCommand command = new CalculateSettlementCommand(
                event.orderId(),
                event.vehicleValue(),
                event.totalDeposits(),
                event.currency());
        calculateSettlement.calculateSettlement(command);
    }
}
