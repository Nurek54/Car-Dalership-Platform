package salon.catalog.application.port.out;

import salon.catalog.application.domain.model.event.DomainEvent;

/**
 * OUTBOUND PORT – publishing domain events outside the context
 * ("EventPublisher" → RabbitMQ). Implements communication between remote contexts
 * (push method via middleware) and eventual consistency (Aggregate Rule 4).
 */
public interface EventPublisher {

    void publish(DomainEvent event);
}
