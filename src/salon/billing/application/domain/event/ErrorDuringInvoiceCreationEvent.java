package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ErrorDuringInvoiceCreationEvent(String orderId, String reason,
                                              UUID eventId, Instant occurredOn) implements DomainEvent {

    public ErrorDuringInvoiceCreationEvent(String orderId, String reason) {
        this(orderId, reason, UUID.randomUUID(), Instant.now());
    }
}
