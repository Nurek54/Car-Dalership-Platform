package integration.sales_and_crm_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import salon.sales.infrastructure.adapter.out.messaging.SalesEventBusAdapter;
import salon.sales.domain.event.OrderPlacedEvent;
import salon.sales.domain.event.OrderActivatedEvent;
import salon.shared.event.DomainEvent;

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
        // AppService przekazuje paczkę złożoną z 2 zdarzeń
        DomainEvent ev1 = new OrderPlacedEvent(UUID.randomUUID(), "ORD-111", Instant.now());
        DomainEvent ev2 = new OrderActivatedEvent(UUID.randomUUID(), "ORD-111", Instant.now());

        // Wołamy publikację całej paczki
        eventBusAdapter.publishAll(List.of(ev1, ev2));

        // THEN (Wtedy): RabbitTemplate wykonuje dokładnie 2 strzały w sieć na odpowiedni routing key
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), eq("order.placed"), eq(ev1));      // zmiana na JSON i wysyłka do RabbitMQ
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), eq("order.activated"), eq(ev2));   // equal
    }

    @Test
    void shouldThrowExceptionWhenMessageBrokerIsDown() {
        // RabbitMQ nie działa, połączenie jest przerwane (Spring rzuca AmqpException)
        doThrow(new AmqpException("Connection refused")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
        DomainEvent ev1 = new OrderPlacedEvent(UUID.randomUUID(), "ORD-222", Instant.now());

        // Błąd infrastruktury musi zostać przepuszczony w górę,
        // aby AppService mógł wycofać transakcję w bazie danych
        assertThatThrownBy(() -> eventBusAdapter.publishAll(List.of(ev1)))
                .isInstanceOf(AmqpException.class);
    }
}