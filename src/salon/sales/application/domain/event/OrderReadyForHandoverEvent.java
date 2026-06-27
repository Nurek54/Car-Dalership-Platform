package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record OrderReadyForHandoverEvent(String orderId, UUID eventId, Instant occurredOn) implements DomainEvent {

    public OrderReadyForHandoverEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }

    public OrderReadyForHandoverEvent(UUID eventId, String orderId, Instant occurredOn) {
        this(orderId, eventId, occurredOn);
    }
}
