package salon.sales.application.handler;

import salon.sales.application.port.out.BillingIntegration;
import salon.sales.application.domain.event.OrderCancelledEvent;

/**
 * Handler of the OrderCancelled event: it passes the cancellation reason to the Billing Context
 * (settling the deposit according to fault for the cancellation).
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
        this.billingPort.processCancelledOrderBilling(event.orderId(), event.reason());
    }
}
