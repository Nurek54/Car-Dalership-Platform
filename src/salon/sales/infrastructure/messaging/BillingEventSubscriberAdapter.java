package salon.sales.infrastructure.messaging;

import salon.billing.domain.event.AdvancePaymentRequestedEvent;
import salon.sales.application.service.OrderAppService;

/**
 * Adapter sterujący (driving) — subskrybent kolejki Rozliczeń w Kontekście Sprzedaży.
 *
 * Odbiera "ZadatekZażądany/Zarejestrowany" (UC-FIR-01) i deleguje do warstwy aplikacji
 * aktywację zamówienia. Wyjątki celowo NIE są łapane: ich wyciek sygnalizuje brokerowi (RabbitMQ),
 * że wiadomość ma trafić do DLQ (Dead Letter Queue).
 */
public class BillingEventSubscriberAdapter {

    private final OrderAppService orderAppService;

    public BillingEventSubscriberAdapter(OrderAppService orderAppService) {
        if (orderAppService == null) {
            throw new IllegalArgumentException("orderAppService must not be null.");
        }
        this.orderAppService = orderAppService;
    }

    public void handleAdvancePaymentRequested(AdvancePaymentRequestedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        orderAppService.activateOrder(event.orderId());
    }
}
