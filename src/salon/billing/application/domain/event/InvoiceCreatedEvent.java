package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record InvoiceCreatedEvent(String orderId, UUID eventId, Instant occurredOn) implements DomainEvent {

    public InvoiceCreatedEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
