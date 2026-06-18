package salon.sales.application.handler;

import salon.sales.application.port.out.BillingIntegration;
import salon.sales.application.domain.event.OrderCancelledEvent;

/**
 * Handler zdarzenia OrderCancelled: przekazuje powód anulowania do Kontekstu Rozliczeń
 * (rozliczenie zadatku wg winy rezygnacji).
 */
public class OrderCancelledEventHandler {

    private final BillingIntegration billingPort;

    public OrderCancelledEventHandler(BillingIntegration billingPort) {
        this.billingPort = billingPort;
    }

    public void handle(OrderCancelledEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        this.billingPort.processCancelledOrderBilling(event.orderId(), event.getReason());
    }
}
