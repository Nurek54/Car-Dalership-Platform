package unit.sales_and_crm_context.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.handler.OrderPlacedEventHandler;
import salon.sales.application.port.out.InventoryIntegration;
import salon.sales.application.domain.event.OrderPlacedEvent;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderPlacedEventHandlerTest {

    @Mock private InventoryIntegration inventoryPort;
    @InjectMocks private OrderPlacedEventHandler eventHandler;

    @Test
    void shouldNotifyInventoryToAllocateProductionSlot() {
        // Zamówienie zostaje złożone
        OrderPlacedEvent event = new OrderPlacedEvent(
                UUID.randomUUID(),
                "ORD-100",
                "SPEC-100",
                Instant.now()
        );

        eventHandler.handle(event);

        // Komunikujemy się z Magazynem, aby zarezerwować slot
        verify(inventoryPort).allocateVehicleOrProductionSlot("ORD-100");
    }
}