package salon.financing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record FinancingApprovedEvent(String orderId,
                                     UUID eventId, Instant occurredOn) implements DomainEvent {

    public FinancingApprovedEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
