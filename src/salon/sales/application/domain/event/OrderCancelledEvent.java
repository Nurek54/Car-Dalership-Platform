package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record OrderCancelledEvent(String orderId, String reason,
                                  UUID eventId, Instant occurredOn) implements DomainEvent {

    public OrderCancelledEvent(String orderId, String reason) {
        this(orderId, reason, UUID.randomUUID(), Instant.now());
    }

    public OrderCancelledEvent(UUID eventId, String orderId, String reason, Instant occurredOn) {
        this(orderId, reason, eventId, occurredOn);
    }
}
