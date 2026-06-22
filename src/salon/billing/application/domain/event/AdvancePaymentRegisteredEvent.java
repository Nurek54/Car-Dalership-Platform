package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AdvancePaymentRegisteredEvent(String orderId, UUID eventId, Instant occurredOn) implements DomainEvent {

    public AdvancePaymentRegisteredEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
