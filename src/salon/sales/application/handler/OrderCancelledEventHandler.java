package salon.sales.application.handler;

import salon.sales.application.domain.event.OrderCancelledEvent;
import salon.sales.application.port.out.BillingIntegration;

/**
 * EVENT HANDLER (Figure 22) — reacts to {@link OrderCancelledEvent} (UC-CRM-03 / A1):
 * passes the cancellation (with reason) to the Billing Context.
 */
public class OrderCancelledEventHandler {

    private final BillingIntegration billingPort;

    public OrderCancelledEventHandler(BillingIntegration billingPort) {
        this.billingPort = billingPort;
    }

    public void handle(OrderCancelledEvent event) {
        this.billingPort.processCancelledOrderBilling(event.orderId(), event.reason());
    }
}
