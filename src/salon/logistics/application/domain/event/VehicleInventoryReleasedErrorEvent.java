package salon.logistics.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-INW-06 / A1: the handover command was rejected because the vehicle was not in the "Ready for handover" state.
 * A compensating event for the Sales Context (the order reverts to READY_FOR_HANDOVER).
 */
public record VehicleInventoryReleasedErrorEvent(String orderId, String reason,
                                                 UUID eventId, Instant occurredOn) implements DomainEvent {

    public VehicleInventoryReleasedErrorEvent(String orderId, String reason) {
        this(orderId, reason, UUID.randomUUID(), Instant.now());
    }
}
