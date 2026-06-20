package salon.financing.infrastructure.in.messaging;

import salon.financing.application.port.in.ProcessFinancing;

/**
 * ADAPTER WEJŚCIOWY (Rysunek 42: EventListener) – subskrybent zdarzeń Kontekstu Finansowania.
 *
 * Mapuje zdarzenia z magistrali na port wejściowy {@link ProcessFinancing}:
 *  - FinancingRequested (z CRM)              -> UC-FIN-01 (złożenie wniosku),
 *  - FinancingDecisionReceivedFromBank (ACL) -> UC-FIN-02 (przetworzenie decyzji).
 *
 * ACL: komunikaty zewnętrzne reprezentujemy jako lokalne rekordy i tłumaczymy na wywołanie portu.
 */
public class FinancingEventListener {

    private final ProcessFinancing processFinancing;

    public FinancingEventListener(ProcessFinancing processFinancing) {
        if (processFinancing == null) {
            throw new IllegalArgumentException("processFinancing must not be null.");
        }
        this.processFinancing = processFinancing;
    }

    public void handleFinancingRequested(FinancingRequested event) {
        if (event == null || event.orderId() == null || event.orderId().isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        this.processFinancing.requestFinancing(event.orderId(), event.customerId());
    }

    public void handleFinancingDecisionReceived(FinancingDecisionReceivedFromBank event) {
        if (event == null || event.orderId() == null || event.orderId().isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        this.processFinancing.processBankDecision(event.orderId(), event.approved());
    }

    /** Lokalna (ACL) reprezentacja zdarzenia FinancingRequested z Kontekstu Sprzedaży i CRM. */
    public record FinancingRequested(String orderId, String customerId) {
    }

    /** Lokalna (ACL) reprezentacja asynchronicznej decyzji z adaptera bankowego. */
    public record FinancingDecisionReceivedFromBank(String orderId, boolean approved) {
    }
}
