package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AdvancePaymentRequestedEvent(String orderId, UUID eventId, Instant occurredOn) implements DomainEvent {

    public AdvancePaymentRequestedEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
