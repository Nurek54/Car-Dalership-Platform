package salon.logistics.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-INW-01 / A1: no free vehicle with the required specification in the yard — the reservation
 * is on hold. Triggers the production-order / deposit-request path (UC-FIR-01).
 */
public record VehicleIsNotOnStockEvent(String orderId,
                                       UUID eventId, Instant occurredOn) implements DomainEvent {

    public VehicleIsNotOnStockEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
