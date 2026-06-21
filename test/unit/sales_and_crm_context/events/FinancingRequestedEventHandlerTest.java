package unit.sales_and_crm_context.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.handler.FinancingRequestedEventHandler;
import salon.financing.application.port.out.BankIntegrationAcl;
import salon.sales.application.domain.event.FinancingRequestedEvent;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancingRequestedEventHandlerTest {

    @Mock private BankIntegrationAcl financePort;
    @InjectMocks private FinancingRequestedEventHandler eventHandler;

    @Test
    void shouldInitiateCreditCheckWhenFinancingIsRequested() {
        // The customer applies for external financing (credit/leasing)
        FinancingRequestedEvent event = new FinancingRequestedEvent(
                UUID.randomUUID(),
                "ORD-600",
                "CUST-600",
                Instant.now()
        );

        eventHandler.handle(event);

        // We call the Financing module port to start the creditworthiness check process
        verify(financePort).startCreditCheckProcess("ORD-600");
    }
}