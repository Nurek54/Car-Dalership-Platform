package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record VehicleHandedOverEvent(String orderId, UUID eventId, Instant occurredOn) implements DomainEvent {

    public VehicleHandedOverEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }

    public VehicleHandedOverEvent(UUID eventId, String orderId, Instant occurredOn) {
        this(orderId, eventId, occurredOn);
    }
}
