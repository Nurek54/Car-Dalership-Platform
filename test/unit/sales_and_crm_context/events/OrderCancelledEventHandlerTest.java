package unit.sales_and_crm_context.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.handler.OrderCancelledEventHandler;
import salon.sales.application.port.out.BillingIntegrationPort;
import salon.sales.domain.event.OrderCancelledEvent;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCancelledEventHandlerTest {

    @Mock private BillingIntegrationPort billingPort;
    @InjectMocks private OrderCancelledEventHandler eventHandler;

    @Test
    void shouldPassCancellationReasonToBillingContext() {
        // Zamówienie zostaje anulowane przez brak wpłaty
        OrderCancelledEvent event = new OrderCancelledEvent(
                UUID.randomUUID(),
                "ORD-300",
                "Brak wpłaty zadatku w terminie",
                Instant.now()
        );

        eventHandler.handle(event);

        verify(billingPort).processCancelledOrderBilling("ORD-300", event.getReason());
    }
}