package salon.sales.application.handler;

import salon.sales.application.port.out.NotificationIntegrationPort;
import salon.sales.application.domain.event.OrderReadyForHandoverEvent;

/**
 * Handler of the OrderReadyForHandover event (UC-CRM-04, step 2): the system generates
 * a notification for the Salesperson to schedule the vehicle pickup date with the customer.
 */
public class OrderReadyForHandoverEventHandler {

    private final NotificationIntegrationPort notificationPort;

    public OrderReadyForHandoverEventHandler(NotificationIntegrationPort notificationPort) {
        this.notificationPort = notificationPort;
    }

    public void handle(OrderReadyForHandoverEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        this.notificationPort.sendAlertToSalesperson(
                event.orderId(),
                "The order is ready for handover. Contact the customer to schedule a date.");
    }
}
