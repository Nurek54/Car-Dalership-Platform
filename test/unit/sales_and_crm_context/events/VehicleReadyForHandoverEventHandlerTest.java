package unit.sales_and_crm_context.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.handler.VehicleReadyForHandoverEventHandler;
import salon.sales.application.service.SalesAppService;
import salon.sales.domain.event.VehicleReadyForHandoverEvent;
import salon.shared.model.OrderId;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleReadyForHandoverEventHandlerTest {

    // Tutaj portem docelowym jest AppService,
    // ponieważ reagujemy na zdarzenie z zewnątrz, aktualizując stan Agregatu
    @Mock private SalesAppService salesAppService;
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

        // CRM odbiera to zdarzenie ze świata
        eventHandler.handle(event);

        // Zlecamy zmianę statusu zamówienia
        verify(salesAppService).markOrderAsReadyForHandover(new OrderId("ORD-700"));
    }
}