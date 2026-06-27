package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record VehicleReadyForHandoverEvent(String vin, String orderId,
                                           UUID eventId, Instant occurredOn) implements DomainEvent {

    public VehicleReadyForHandoverEvent(String vin, String orderId) {
        this(vin, orderId, UUID.randomUUID(), Instant.now());
    }

    public VehicleReadyForHandoverEvent(UUID eventId, String vin, String orderId, Instant occurredOn) {
        this(vin, orderId, eventId, occurredOn);
    }
}
