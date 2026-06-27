package salon.logistics.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record VehicleIsNotOnStockEvent(String orderId,
                                       UUID eventId, Instant occurredOn) implements DomainEvent {

    public VehicleIsNotOnStockEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
