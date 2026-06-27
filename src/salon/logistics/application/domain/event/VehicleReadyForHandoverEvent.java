package salon.logistics.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record VehicleReadyForHandoverEvent(String orderId, String vin,
                                           UUID eventId, Instant occurredOn) implements DomainEvent {

    public VehicleReadyForHandoverEvent(String orderId, String vin) {
        this(orderId, vin, UUID.randomUUID(), Instant.now());
    }
}
