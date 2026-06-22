package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-CRM-05: the order has been completed (the vehicle was physically handed over).
 */
public record OrderCompletedEvent(String orderId, UUID eventId, Instant occurredOn) implements DomainEvent {

    public OrderCompletedEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }

    public OrderCompletedEvent(UUID eventId, String orderId, Instant occurredOn) {
        this(orderId, eventId, occurredOn);
    }
}
