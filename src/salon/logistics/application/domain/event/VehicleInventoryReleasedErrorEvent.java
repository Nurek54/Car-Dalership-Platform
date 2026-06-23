package salon.logistics.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record VehicleInventoryReleasedErrorEvent(String orderId, String reason,
                                                 UUID eventId, Instant occurredOn) implements DomainEvent {

    public VehicleInventoryReleasedErrorEvent(String orderId, String reason) {
        this(orderId, reason, UUID.randomUUID(), Instant.now());
    }
}
