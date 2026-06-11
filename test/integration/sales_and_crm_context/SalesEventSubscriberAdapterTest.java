package integration.sales_and_crm_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import salon.sales.infrastructure.adapter.in.messaging.SalesEventSubscriberAdapter;
import salon.sales.application.service.SalesAppService;
import salon.sales.domain.event.VehicleReadyForHandoverEvent;
import salon.sales.domain.exception.OrderNotFoundException;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(classes = SalesEventSubscriberAdapter.class)
class SalesEventSubscriberAdapterTest {

    @Autowired private SalesEventSubscriberAdapter subscriberAdapter;
    @MockBean private SalesAppService salesAppService;

    @Test
    void shouldConsumeMessageAndTriggerAppServiceSuccessfully() {
        // Z Inwentarza przypływa poprawne zdarzenie
        VehicleReadyForHandoverEvent incomingEvent = new VehicleReadyForHandoverEvent(
                UUID.randomUUID(), "VIN-999", "ORD-123", Instant.now()
        );

        // Adapter nasłuchujący wyłapuje komunikat
        subscriberAdapter.onVehicleReadyForHandover(incomingEvent);

        // Zleca wykonanie pracy do AppService
        verify(salesAppService).markOrderAsReadyForHandover(any());
    }

    @Test
    void shouldHandleAppServiceExceptionGracefullyWithoutCrashingListener() {
        // AppService odrzuca event, ponieważ zamówienie ORD-UNKNOWN nie istnieje w naszej bazie
        VehicleReadyForHandoverEvent incomingEvent = new VehicleReadyForHandoverEvent(
                UUID.randomUUID(), "VIN-000", "ORD-UNKNOWN", Instant.now()
        );
        doThrow(new OrderNotFoundException("ORD-UNKNOWN")).when(salesAppService).markOrderAsReadyForHandover(any());

        // Adapter wyłapuje błąd i zapisuje go w logach
        // Aplikacja nie może się zablkokować, aby nie zablokować całej kolejki
        assertDoesNotThrow(() -> subscriberAdapter.onVehicleReadyForHandover(incomingEvent));

        // Weryfikujemy, że AppService faktycznie został wezwany
        verify(salesAppService).markOrderAsReadyForHandover(any());
    }
}