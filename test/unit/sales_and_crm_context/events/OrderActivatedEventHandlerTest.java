package unit.sales_and_crm_context.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.handler.OrderActivatedEventHandler;
import salon.sales.application.port.out.ManufacturingIntegrationPort;
import salon.sales.application.domain.event.OrderActivatedEvent;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderActivatedEventHandlerTest {

    @Mock private ManufacturingIntegrationPort manufacturingPort;
    @InjectMocks private OrderActivatedEventHandler eventHandler;

    @Test
    void shouldTriggerRealizationProcessWhenActivated() {
        // Zaksięgowano wpłatę, aktywacja udana
        OrderActivatedEvent event = new OrderActivatedEvent(
                UUID.randomUUID(),
                "ORD-200",
                Instant.now()
        );

        eventHandler.handle(event);

        // Rusza realizacja (np. zlecenie na taśmę produkcyjną)
        verify(manufacturingPort).startVehicleRealization("ORD-200");
    }
}