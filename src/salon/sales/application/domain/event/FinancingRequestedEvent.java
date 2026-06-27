package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record FinancingRequestedEvent(String orderId, String customerId,
                                      UUID eventId, Instant occurredOn) implements DomainEvent {

    public FinancingRequestedEvent(String orderId, String customerId) {
        this(orderId, customerId, UUID.randomUUID(), Instant.now());
    }

    public FinancingRequestedEvent(UUID eventId, String orderId, String customerId, Instant occurredOn) {
        this(orderId, customerId, eventId, occurredOn);
    }
}
