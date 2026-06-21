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
        // A valid event arrives from Inventory
        VehicleReadyForHandoverEvent incomingEvent = new VehicleReadyForHandoverEvent(
                UUID.randomUUID(), "VIN-999", "ORD-123", Instant.now()
        );

        // The listening adapter catches the message
        subscriberAdapter.onVehicleReadyForHandover(incomingEvent);

        // Zleca wykonanie pracy do AppService
        verify(salesAppService).markOrderAsReadyForHandover(any());
    }

    @Test
    void shouldHandleAppServiceExceptionGracefullyWithoutCrashingListener() {
        // The AppService rejects the event because order ORD-UNKNOWN does not exist in our database
        VehicleReadyForHandoverEvent incomingEvent = new VehicleReadyForHandoverEvent(
                UUID.randomUUID(), "VIN-000", "ORD-UNKNOWN", Instant.now()
        );
        doThrow(new OrderNotFoundException("ORD-UNKNOWN")).when(salesAppService).markOrderAsReadyForHandover(any());

        // The adapter catches the error and records it in the logs
        // The application must not lock up, so as not to block the whole queue
        assertDoesNotThrow(() -> subscriberAdapter.onVehicleReadyForHandover(incomingEvent));

        // We verify that the AppService was actually called
        verify(salesAppService).markOrderAsReadyForHandover(any());
    }
}