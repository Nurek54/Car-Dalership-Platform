package integration.sales_and_crm_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import salon.sales.infrastructure.in.messaging.SalesEventSubscriberAdapter;
import salon.sales.application.service.SalesService;
import salon.sales.application.domain.event.VehicleReadyForHandoverEvent;
import salon.sales.application.domain.exception.OrderNotFoundException;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(classes = SalesEventSubscriberAdapter.class)
class SalesEventSubscriberAdapterTest {

    @Autowired private SalesEventSubscriberAdapter subscriberAdapter;
    @MockBean private SalesService salesAppService;

    @Test
    void shouldConsumeMessageAndTriggerAppServiceSuccessfully() {
        // Z Magazynu przychodzi poprawne zdarzenie
        VehicleReadyForHandoverEvent incomingEvent = new VehicleReadyForHandoverEvent(
                UUID.randomUUID(), "VIN-999", "ORD-123", Instant.now()
        );

        // Nasłuchujący adapter przechwytuje wiadomość
        subscriberAdapter.onVehicleReadyForHandover(incomingEvent);

        // Zleca wykonanie pracy do AppService
        verify(salesAppService).markOrderAsReadyForHandover(any());
    }

    @Test
    void shouldHandleAppServiceExceptionGracefullyWithoutCrashingListener() {
        // AppService odrzuca zdarzenie, bo zamówienie ORD-UNKNOWN nie istnieje w naszej bazie
        VehicleReadyForHandoverEvent incomingEvent = new VehicleReadyForHandoverEvent(
                UUID.randomUUID(), "VIN-000", "ORD-UNKNOWN", Instant.now()
        );
        doThrow(new OrderNotFoundException("ORD-UNKNOWN")).when(salesAppService).markOrderAsReadyForHandover(any());

        // Adapter przechwytuje błąd i zapisuje go w logach
        // Aplikacja nie może się zaciąć, żeby nie zablokować całej kolejki
        assertDoesNotThrow(() -> subscriberAdapter.onVehicleReadyForHandover(incomingEvent));

        // Sprawdzamy, że AppService faktycznie został wywołany
        verify(salesAppService).markOrderAsReadyForHandover(any());
    }
}