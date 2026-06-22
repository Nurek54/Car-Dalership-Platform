package salon.sales.application.handler;

import salon.sales.application.domain.event.OrderReadyForHandoverEvent;
import salon.sales.application.port.out.NotificationIntegrationPort;

/**
 * EVENT HANDLER (Figure 22) — reacts to {@link OrderReadyForHandoverEvent} (UC-CRM-04):
 * alerts the salesperson to contact the customer and schedule the handover.
 */
public class OrderReadyForHandoverEventHandler {

    private final NotificationIntegrationPort notificationPort;

    public OrderReadyForHandoverEventHandler(NotificationIntegrationPort notificationPort) {
        this.notificationPort = notificationPort;
    }

    public void handle(OrderReadyForHandoverEvent event) {
        // The system sends a notification to the salesperson to call the customer
        this.notificationPort.sendAlertToSalesperson(
                event.orderId(),
                "The order is ready for handover. Contact the customer to schedule a date.");
    }
}
