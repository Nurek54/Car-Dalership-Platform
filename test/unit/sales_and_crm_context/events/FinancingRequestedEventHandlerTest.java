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
        // Klient wnioskuje o zewnętrzne finansowanie (kredyt/leasing)
        FinancingRequestedEvent event = new FinancingRequestedEvent(
                UUID.randomUUID(),
                "ORD-600",
                "CUST-600",
                Instant.now()
        );

        eventHandler.handle(event);

        // Wołamy port modułu Finansowania, aby rozpocząć proces badania zdolności kredytowej
        verify(financePort).startCreditCheckProcess("ORD-600");
    }
}