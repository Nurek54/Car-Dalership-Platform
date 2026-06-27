package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record OrderActivatedEvent(String orderId, UUID eventId, Instant occurredOn) implements DomainEvent {

    public OrderActivatedEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }

    public OrderActivatedEvent(UUID eventId, String orderId, Instant occurredOn) {
        this(orderId, eventId, occurredOn);
    }
}
