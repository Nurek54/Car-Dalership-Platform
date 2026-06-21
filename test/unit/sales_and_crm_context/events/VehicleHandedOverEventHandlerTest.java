package unit.sales_and_crm_context.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.handler.VehicleHandedOverEventHandler;
import salon.sales.application.port.out.BillingIntegration;
import salon.sales.application.port.out.AfterSalesIntegrationPort;
import salon.sales.application.domain.event.VehicleHandedOverEvent;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleHandedOverEventHandlerTest {

    @Mock private BillingIntegration billingPort;
    @Mock private AfterSalesIntegrationPort afterSalesPort;
    @InjectMocks private VehicleHandedOverEventHandler eventHandler;

    @Test
    void shouldNotifyBillingAndAfterSalesWhenVehicleIsHandedOver() {
        // The customer drove off with the car from the dealership
        VehicleHandedOverEvent event = new VehicleHandedOverEvent(
                UUID.randomUUID(),
                "ORD-999",
                Instant.now()
        );

        eventHandler.handle(event);

        // We instruct the Billing context to close the balance
        verify(billingPort).closeOrderBalance("ORD-999");
    }
}