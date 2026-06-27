package salon.sales.application.handler;

import salon.sales.application.domain.event.OrderCancelledEvent;
import salon.sales.application.port.out.BillingIntegration;

public class OrderCancelledEventHandler {

    private final BillingIntegration billingPort;

    public OrderCancelledEventHandler(BillingIntegration billingPort) {
        this.billingPort = billingPort;
    }

    public void handle(OrderCancelledEvent event) {
        this.billingPort.processCancelledOrderBilling(event.orderId(), event.reason());
    }
}
