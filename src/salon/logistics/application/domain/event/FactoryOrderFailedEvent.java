package salon.logistics.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record FactoryOrderFailedEvent(UUID eventId,
                                      String orderId,
                                      String reason,
                                      Instant occurredOn) implements DomainEvent {

    public FactoryOrderFailedEvent(String orderId, String reason) {
        this(UUID.randomUUID(), orderId, reason, Instant.now());
    }
}
