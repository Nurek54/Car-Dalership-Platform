package salon.financing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record FinancingApplicationFailedEvent(String orderId, String reason,
                                              UUID eventId, Instant occurredOn) implements DomainEvent {

    public FinancingApplicationFailedEvent(String orderId, String reason) {
        this(orderId, reason, UUID.randomUUID(), Instant.now());
    }
}
