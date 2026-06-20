package salon.financing.infrastructure.in.messaging;

import salon.financing.application.port.in.ProcessFinancing;
import salon.sales.application.domain.event.FinancingRequestedEvent;

/**
 * Adapter sterujacy (driving) — subskrybent magistrali zdarzen w Kontekscie Finansowania
 * (PDF rozdz. 3.6.3, "FinancingEventListener").
 *
 * Przechwytuje FinancingRequestedEvent wyemitowany przez Kontekst Sprzedazy (UC-CRM-03, krok 5)
 * i asynchronicznie wyzwala UC-FIN-01 (zlozenie wniosku przez ACL banku). Adapter sam lapie
 * wyjatki, aby pojedynczy zatruty komunikat nie zablokowal calej kolejki.
 */
public class FinancingEventListener {

    private final ProcessFinancing financingRequest;

    public FinancingEventListener(ProcessFinancing financingRequest) {
        if (financingRequest == null) {
            throw new IllegalArgumentException("financingRequest must not be null.");
        }
        this.financingRequest = financingRequest;
    }

    /** UC-FIN-01: zdarzenie z CRM -> zlozenie wniosku o finansowanie. */
    public void onFinancingRequested(FinancingRequestedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        if (event.orderId() == null || event.orderId().isBlank()) {
            throw new IllegalArgumentException("orderId jest wymagany");
        }
        try {
            financingRequest.submitFinancing(event.orderId(), event.customerId());
        } catch (Exception e) {
            System.err.println("[FinancingEventListener] Nie udalo sie przetworzyc zdarzenia "
                    + event.eventId() + ": " + e.getMessage());
        }
    }
}
