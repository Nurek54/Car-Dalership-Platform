package unit.sales_and_crm_context.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.handler.OrderReadyForHandoverEventHandler;
import salon.sales.application.port.out.NotificationIntegrationPort;
import salon.sales.domain.event.OrderReadyForHandoverEvent;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderReadyForHandoverEventHandlerTest {

    @Mock private NotificationIntegrationPort notificationPort;
    @InjectMocks private OrderReadyForHandoverEventHandler eventHandler;

    @Test
    void shouldNotifySalespersonToScheduleHandover() {
        // Zamówienie spełniło wszystkie wymogi i jest gotowe do odbioru
        OrderReadyForHandoverEvent event = new OrderReadyForHandoverEvent(
                UUID.randomUUID(),
                "ORD-800",
                Instant.now()
        );

        eventHandler.handle(event);

        // System wysyła ppowiadomienie do handlowca, aby zadzwonił do klienta
        verify(notificationPort).sendAlertToSalesperson(
                "ORD-800",
                "Zamówienie jest gotowe do wydania. Skontaktuj się z klientem, aby umówić termin."
        );
    }
}