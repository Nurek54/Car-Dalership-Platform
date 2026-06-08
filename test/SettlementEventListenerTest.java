import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import salon.billing.application.service.SettlementAppService;
import salon.billing.domain.model.settlement.Settlement;
import salon.billing.domain.model.settlement.SettlementFactory;
import salon.billing.domain.model.settlement.SettlementStatus;
import salon.billing.infrastructure.messaging.OrderReadyForSettlementEvent;
import salon.billing.infrastructure.messaging.SettlementEventListener;
import salon.billing.infrastructure.mock.InMemorySettlementRepository;
import salon.billing.infrastructure.mock.InProcessEventPublisherAdapter;
import salon.billing.infrastructure.mock.PaymentGatewayMockAdapter;
import salon.shared.model.OrderId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettlementEventListenerTest {

    private InMemorySettlementRepository repository;
    private SettlementEventListener listener;

    private OrderReadyForSettlementEvent sampleEvent(UUID eventId) {
        return new OrderReadyForSettlementEvent(
                eventId, "ORDER-1", new BigDecimal("100000"), "PLN", Instant.now());
    }

    private SettlementEventListener freshListener() {
        this.repository = new InMemorySettlementRepository();
        SettlementAppService appService = new SettlementAppService(
                this.repository, new SettlementFactory(),
                new InProcessEventPublisherAdapter(), new PaymentGatewayMockAdapter());
        return new SettlementEventListener(appService);
    }

    @Test
    @DisplayName("Event initializes the settlement aggregate with mapped data")
    void initializesSettlementWithMappedData() {
        this.listener = freshListener();

        this.listener.on(sampleEvent(UUID.randomUUID()));

        Optional<Settlement> saved = this.repository.findByOrderId(new OrderId("ORDER-1"));
        assertTrue(saved.isPresent());
        assertEquals(0, new BigDecimal("100000").compareTo(saved.get().getTotalAmount().amount()));
        assertEquals("PLN", saved.get().getTotalAmount().currency());
        assertEquals(SettlementStatus.OPEN, saved.get().getStatus());
    }

    @Test
    @DisplayName("Duplicate event (same eventId) is processed only once")
    void ignoresDuplicateEvent() {
        this.listener = freshListener();

        UUID eventId = UUID.randomUUID();
        this.listener.on(sampleEvent(eventId));
        this.listener.on(sampleEvent(eventId));

        assertEquals(1, this.repository.findAll().size());
    }
}
