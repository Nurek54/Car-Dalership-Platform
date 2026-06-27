package integration.catalog_and_configurator_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.event.CatalogUpdated;
import salon.catalog.application.domain.model.event.SpecificationCompleted;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;
import salon.catalog.application.domain.model.specification.SpecificationId;
import salon.catalog.infrastructure.out.messaging.MessageBroker;
import salon.catalog.infrastructure.out.messaging.RabbitMqEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/** Integracja adaptera wyjściowego EventPublisher — publikacja zdarzeń na broker (routing key). */
@SpringBootTest(classes = RabbitMqEventPublisher.class)
class RabbitMqEventPublisherTest {

    @Autowired private RabbitMqEventPublisher publisher;
    @MockBean private MessageBroker broker;

    @Test
    void shouldPublishCatalogUpdatedWithRoutingKey() {
        // Zdarzenie aktualizacji cennika
        publisher.publish(new CatalogUpdated(CatalogId.generate(), ModelYear.of(2025), 2, Instant.now()));

        // Adapter tłumaczy zdarzenie na wiadomość brokera z kluczem trasowania catalog.<nazwa>
        verify(broker).send(eq("catalog.CatalogUpdated"), contains("\"version\":2"));
    }

    @Test
    void shouldPublishSpecificationCompletedWithRoutingKey() {
        // Zdarzenie finalizacji specyfikacji
        publisher.publish(new SpecificationCompleted(
                SpecificationId.generate(), CatalogId.generate(),
                Money.of(new BigDecimal("12000"), "PLN"), List.of(OptionCode.of("B2")), Instant.now()));

        verify(broker).send(eq("catalog.SpecificationCompleted"), contains("\"specificationId\""));
    }
}
