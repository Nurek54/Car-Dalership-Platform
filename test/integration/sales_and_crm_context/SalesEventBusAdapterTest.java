package integration.sales_and_crm_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import salon.sales.infrastructure.out.messaging.SalesEventBusAdapter;
import salon.sales.application.domain.event.OrderPlacedEvent;
import salon.sales.application.domain.event.OrderActivatedEvent;
import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = SalesEventBusAdapter.class)
class SalesEventBusAdapterTest {

    @Autowired private SalesEventBusAdapter eventBusAdapter;
    @MockBean private RabbitTemplate rabbitTemplate;

    @Test
    void shouldPublishMultipleEventsToMessageBroker() {
        // The AppService passes a batch consisting of 2 events
        DomainEvent ev1 = new OrderPlacedEvent(UUID.randomUUID(), "ORD-111", "SPEC-111", Instant.now());
        DomainEvent ev2 = new OrderActivatedEvent(UUID.randomUUID(), "ORD-111", Instant.now());

        // We call the publication of the whole batch
        eventBusAdapter.publishAll(List.of(ev1, ev2));

        // THEN: RabbitTemplate makes exactly 2 network calls to the appropriate routing key
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), eq("order.placed"), eq(ev1));      // conversion to JSON and sending to RabbitMQ
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), eq("order.activated"), eq(ev2));   // equal
    }

    @Test
    void shouldThrowExceptionWhenMessageBrokerIsDown() {
        // RabbitMQ is down, the connection is broken (Spring throws AmqpException)
        doThrow(new AmqpException("Connection refused")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
        DomainEvent ev1 = new OrderPlacedEvent(UUID.randomUUID(), "ORD-222", "SPEC-222", Instant.now());

        // The infrastructure error must be propagated upward,
        // so that the AppService can roll back the database transaction
        assertThatThrownBy(() -> eventBusAdapter.publishAll(List.of(ev1)))
                .isInstanceOf(AmqpException.class);
    }
}