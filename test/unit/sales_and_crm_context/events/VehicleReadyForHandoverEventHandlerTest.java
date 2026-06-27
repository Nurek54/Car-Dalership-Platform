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

    // Tutaj docelowym portem jest AppService,
    // ponieważ reagujemy na zdarzenie zewnętrzne, aktualizując stan agregatu
    @Mock private SalesService salesAppService;
    @InjectMocks private VehicleReadyForHandoverEventHandler eventHandler;

    @Test
    void shouldMarkOrderAsReadyWhenVehicleIsPhysicallyReady() {
        // System logistyczny zgłasza gotowość pojazdu
        VehicleReadyForHandoverEvent event = new VehicleReadyForHandoverEvent(
                UUID.randomUUID(),
                "VEH-999-VIN",
                "ORD-700",
                Instant.now()
        );

        // CRM odbiera to zdarzenie ze świata zewnętrznego
        eventHandler.handle(event);

        // Instruujemy zmianę statusu zamówienia
        verify(salesAppService).markOrderAsReadyForHandover(new OrderId("ORD-700"));
    }
}