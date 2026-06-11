package unit.sales_and_crm_context.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.handler.FinancingRequestedEventHandler;
import salon.financing.application.port.out.BankIntegrationAclPort;
import salon.sales.domain.event.FinancingRequestedEvent;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancingRequestedEventHandlerTest {

    @Mock private BankIntegrationAclPort financePort;
    @InjectMocks private FinancingRequestedEventHandler eventHandler;

    @Test
    void shouldInitiateCreditCheckWhenFinancingIsRequested() {
        // Klient wnioskuje o finansowanie zewnętrzne (kredyt/leasing)
        FinancingRequestedEvent event = new FinancingRequestedEvent(
                UUID.randomUUID(),
                "ORD-600",
                Instant.now()
        );

        eventHandler.handle(event);

        // Wywołujemy port modułu Finansowego, aby uruchomił proces sprawdzania zdolności
        verify(financePort).startCreditCheckProcess("ORD-600");
    }
}