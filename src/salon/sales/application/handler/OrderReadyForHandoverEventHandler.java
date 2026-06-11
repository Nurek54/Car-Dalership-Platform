package salon.sales.application.handler;

import salon.sales.application.port.out.NotificationIntegrationPort;
import salon.sales.domain.event.OrderReadyForHandoverEvent;

/**
 * Handler zdarzenia OrderReadyForHandover (UC-CRM-04, krok 2): system generuje
 * powiadomienie dla Handlowca, aby umówił z klientem termin odbioru pojazdu.
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
                "Zamówienie jest gotowe do wydania. Skontaktuj się z klientem, aby umówić termin.");
    }
}
