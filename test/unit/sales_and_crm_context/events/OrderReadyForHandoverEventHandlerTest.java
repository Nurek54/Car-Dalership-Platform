package unit.sales_and_crm_context.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.handler.OrderReadyForHandoverEventHandler;
import salon.sales.application.port.out.NotificationIntegrationPort;
import salon.sales.application.domain.event.OrderReadyForHandoverEvent;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderReadyForHandoverEventHandlerTest {

    @Mock private NotificationIntegrationPort notificationPort;
    @InjectMocks private OrderReadyForHandoverEventHandler eventHandler;

    @Test
    void shouldNotifySalespersonToScheduleHandover() {
        // The order met all requirements and is ready for handover
        OrderReadyForHandoverEvent event = new OrderReadyForHandoverEvent(
                UUID.randomUUID(),
                "ORD-800",
                Instant.now()
        );

        eventHandler.handle(event);

        // The system sends a notification to the salesperson to call the customer
        verify(notificationPort).sendAlertToSalesperson(
                "ORD-800",
                "The order is ready for handover. Contact the customer to schedule a date."
        );
    }
}