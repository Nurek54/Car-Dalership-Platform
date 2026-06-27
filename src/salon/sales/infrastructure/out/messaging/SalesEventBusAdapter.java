package salon.sales.infrastructure.out.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import salon.common.application.EventPublisher;
import salon.common.event.DomainEvent;
import salon.sales.application.domain.event.BankTransferDeclaredEvent;
import salon.sales.application.domain.event.ConfiguratorSessionInitiatedEvent;
import salon.sales.application.domain.event.FinancingRequestedEvent;
import salon.sales.application.domain.event.OfferCreatedEvent;
import salon.sales.application.domain.event.OrderActivatedEvent;
import salon.sales.application.domain.event.OrderCancelledEvent;
import salon.sales.application.domain.event.OrderCompletedEvent;
import salon.sales.application.domain.event.OrderPlacedEvent;
import salon.sales.application.domain.event.OrderReadyForHandoverEvent;
import salon.sales.application.domain.event.VehicleHandedOverEvent;
import salon.sales.application.domain.event.VehicleReadyForHandoverEvent;

import java.util.List;
import java.util.Map;

@Component
@Primary
public class SalesEventBusAdapter implements EventPublisher {

    public static final String EXCHANGE = "sales.events.exchange";

    private static final Map<Class<?>, String> ROUTING_KEYS = Map.ofEntries(
            Map.entry(OrderPlacedEvent.class, "order.placed"),
            Map.entry(OrderActivatedEvent.class, "order.activated"),
            Map.entry(OrderCancelledEvent.class, "order.cancelled"),
            Map.entry(OrderReadyForHandoverEvent.class, "order.ready_for_handover"),
            Map.entry(OrderCompletedEvent.class, "order.completed"),
            Map.entry(ConfiguratorSessionInitiatedEvent.class, "configurator.session.initiated"),
            Map.entry(VehicleHandedOverEvent.class, "vehicle.handed_over"),
            Map.entry(VehicleReadyForHandoverEvent.class, "vehicle.ready_for_handover"),
            Map.entry(BankTransferDeclaredEvent.class, "bank_transfer.declared"),
            Map.entry(FinancingRequestedEvent.class, "financing.requested"),
            Map.entry(OfferCreatedEvent.class, "offer.created"));

    private final RabbitTemplate rabbitTemplate;

    public SalesEventBusAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(DomainEvent event) {
        this.rabbitTemplate.convertAndSend(EXCHANGE, routingKeyFor(event), event);
    }

    @Override
    public void publishAll(List<DomainEvent> events) {
        if (events == null) {
            return;
        }
        for (DomainEvent event : events) {
            publish(event);
        }
    }

    private String routingKeyFor(DomainEvent event) {
        return ROUTING_KEYS.getOrDefault(event.getClass(),
                event.getClass().getSimpleName().toLowerCase());
    }
}
