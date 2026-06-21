package unit.sales_and_crm_context.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.handler.VehicleReadyForHandoverEventHandler;
import salon.sales.application.service.SalesService;
import salon.sales.application.domain.event.VehicleReadyForHandoverEvent;
import salon.common.model.OrderId;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleReadyForHandoverEventHandlerTest {

    // Here the target port is the AppService,
    // because we react to an external event, updating the Aggregate state
    @Mock private SalesService salesAppService;
    @InjectMocks private VehicleReadyForHandoverEventHandler eventHandler;

    @Test
    void shouldMarkOrderAsReadyWhenVehicleIsPhysicallyReady() {
        // The logistics system reports the vehicle's readiness
        VehicleReadyForHandoverEvent event = new VehicleReadyForHandoverEvent(
                UUID.randomUUID(),
                "VEH-999-VIN",
                "ORD-700",
                Instant.now()
        );

        // CRM receives this event from the outside world
        eventHandler.handle(event);

        // We instruct the change of the order status
        verify(salesAppService).markOrderAsReadyForHandover(new OrderId("ORD-700"));
    }
}