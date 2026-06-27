package integration.billing_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import salon.billing.application.port.in.ProcessPayment;
import salon.billing.infrastructure.in.messaging.OrderReadyForSettlementEvent;
import salon.billing.infrastructure.in.messaging.SettlementEventListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/** Adapter nasłuchujący OrderReadyForSettlement — inicjalizacja należności z deduplikacją zdarzeń. */
@SpringBootTest(classes = SettlementEventListener.class)
class SettlementEventListenerTest {

    @Autowired private SettlementEventListener listener;
    @MockBean private ProcessPayment processPayment;

    @Test
    void shouldInitializeSettlementOnce_evenWhenEventDelivieredTwice() {
        // To samo zdarzenie (ten sam eventId) dostarczone dwukrotnie
        UUID eventId = UUID.randomUUID();
        OrderReadyForSettlementEvent event = new OrderReadyForSettlementEvent(
                eventId, "ORD-1", new BigDecimal("100000"), "PLN", Instant.now());

        listener.on(event);
        listener.on(event);

        // Idempotencja: należność inicjalizowana tylko raz
        verify(processPayment, times(1)).initializeSettlement(any(), any());
    }
}
