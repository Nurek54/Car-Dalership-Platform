package salon.logistics.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-INW-01: a free vehicle from the yard was hard-reserved (a lock on the VIN) for the order.
 */
public record VehicleReservedFromStockEvent(String orderId, String vin,
                                            UUID eventId, Instant occurredOn) implements DomainEvent {

    public VehicleReservedFromStockEvent(String orderId, String vin) {
        this(orderId, vin, UUID.randomUUID(), Instant.now());
    }
}
