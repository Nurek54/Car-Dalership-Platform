package salon.sales.application.handler;

import salon.sales.application.domain.event.OrderReadyForHandoverEvent;
import salon.sales.application.port.out.NotificationIntegrationPort;

public class OrderReadyForHandoverEventHandler {

    private final NotificationIntegrationPort notificationPort;

    public OrderReadyForHandoverEventHandler(NotificationIntegrationPort notificationPort) {
        this.notificationPort = notificationPort;
    }

    public void handle(OrderReadyForHandoverEvent event) {
        
        this.notificationPort.sendAlertToSalesperson(
                event.orderId(),
                "The order is ready for handover. Contact the customer to schedule a date.");
    }
}
