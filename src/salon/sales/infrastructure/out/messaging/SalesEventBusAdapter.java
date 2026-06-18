package salon.sales.infrastructure.out.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import salon.common.application.EventPublisher;
import salon.common.event.DomainEvent;

import java.util.List;

/**
 * Adapter wyjściowy (EventBusAdapter) — fizyczny publikator Zdarzeń Dziedziny na RabbitMQ
 * (PDF rozdz. 3.1.1: "EventBusAdapter: fizyczny publikator zdarzeń, np. RabbitTemplate").
 *
 * Routing key wyprowadzany z nazwy zdarzenia (np. OrderPlacedEvent -> "order.placed").
 * Błąd brokera jest przepuszczany w górę, aby usługa aplikacyjna mogła wycofać transakcję.
 */
@Component
public class SalesEventBusAdapter implements EventPublisher {

    /** Wspólna wymiana zdarzeń Kontekstu Sprzedaży (kanwa: Outbound Communication). */
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

    /** Mapowanie nazw zdarzeń na etykiety komunikatów (routing keys). */
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
                // Konwencja zapasowa: OrderXyzEvent -> "order.xyz" (pierwszy człon + snake_case reszty)
                String base = name.endsWith("Event") ? name.substring(0, name.length() - 5) : name;
                String snake = base.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
                int cut = snake.indexOf('_');
                return cut < 0 ? snake : snake.substring(0, cut) + "." + snake.substring(cut + 1);
        }
    }
}
