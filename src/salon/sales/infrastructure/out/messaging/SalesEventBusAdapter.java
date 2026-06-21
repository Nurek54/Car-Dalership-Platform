package salon.sales.infrastructure.out.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import salon.common.application.EventPublisher;
import salon.common.event.DomainEvent;

import java.util.List;

/**
 * Outbound adapter (EventBusAdapter) — the physical publisher of Domain Events to RabbitMQ
 * (PDF chapter 3.1.1: "EventBusAdapter: the physical event publisher, e.g. RabbitTemplate").
 *
 * The routing key is derived from the event name (e.g. OrderPlacedEvent -> "order.placed").
 * A broker error is propagated upward so the application service can roll back the transaction.
 */
@Component
public class SalesEventBusAdapter implements EventPublisher {

    /** The shared event exchange of the Sales Context (canvas: Outbound Communication). */
    public static final String EXCHANGE = "sales.events.exchange";

    private final RabbitTemplate rabbitTemplate;

    public SalesEventBusAdapter(RabbitTemplate rabbitTemplate) {
        if (rabbitTemplate == null) {
            throw new IllegalArgumentException("rabbitTemplate must not be null.");
        }
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
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

    /** Mapping of event names to message labels (routing keys). */
    static String routingKeyFor(DomainEvent event) {
        String name = event.getClass().getSimpleName();
        switch (name) {
            case "OrderPlacedEvent":             return "order.placed";
            case "OrderActivatedEvent":          return "order.activated";
            case "OrderCancelledEvent":          return "order.cancelled";
            case "OrderCompletedEvent":          return "order.completed";
            case "OrderReadyForHandoverEvent":   return "order.ready_for_handover";
            case "VehicleHandedOverEvent":       return "vehicle.handed_over";
            case "ConfiguratorSessionInitiatedEvent": return "configurator.session.initiated";
            case "BankTransferDeclaredEvent":    return "bank_transfer.declared";
            case "FinancingRequestedEvent":      return "financing.requested";
            default:
                // Fallback convention: OrderXyzEvent -> "order.xyz" (first segment + snake_case of the rest)
                String base = name.endsWith("Event") ? name.substring(0, name.length() - 5) : name;
                String snake = base.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
                int cut = snake.indexOf('_');
                return cut < 0 ? snake : snake.substring(0, cut) + "." + snake.substring(cut + 1);
        }
    }
}
