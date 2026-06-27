package integration.billing_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import salon.billing.application.domain.event.InvoiceCreatedEvent;
import salon.billing.application.domain.event.PaymentRegisteredEvent;
import salon.billing.application.domain.event.SettlementCompletedEvent;
import salon.billing.infrastructure.out.mock.InProcessEventPublisherAdapter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Integracja adaptera publikacji zdarzeń (in-process) — rejestrowanie i odczyt wyemitowanych zdarzeń. */
@SpringBootTest(classes = InProcessEventPublisherAdapter.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class InProcessEventPublisherAdapterTest {

    @Autowired private InProcessEventPublisherAdapter eventPublisher;

    @Test
    void shouldPublishSingleEvent() {
        // Pojedyncze zdarzenie zostaje zarejestrowane w kolejności publikacji
        eventPublisher.publish(new InvoiceCreatedEvent("ORD-1"));

        assertThat(eventPublisher.publishedEvents()).hasSize(1);
        assertThat(eventPublisher.publishedEvents().get(0)).isInstanceOf(InvoiceCreatedEvent.class);
    }

    @Test
    void shouldPublishWholeBatchViaPublishAll() {
        // Cała paczka zdarzeń (publishAll) trafia po kolei na "magistralę"
        eventPublisher.publishAll(List.of(
                new PaymentRegisteredEvent("ORD-2"),
                new SettlementCompletedEvent("ORD-2")));

        assertThat(eventPublisher.publishedEvents())
                .hasSize(2)
                .hasAtLeastOneElementOfType(PaymentRegisteredEvent.class)
                .hasAtLeastOneElementOfType(SettlementCompletedEvent.class);
    }
}
