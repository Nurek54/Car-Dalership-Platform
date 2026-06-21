package salon.logistics.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-INW-04: automatic removal of the lock after the payment deadline is exceeded —
 * the vehicle returns to the yard for resale.
 */
public record VehicleReservationCancelledEvent(String orderId, String vin,
                                               UUID eventId, Instant occurredOn) implements DomainEvent {

    public VehicleReservationCancelledEvent(String orderId, String vin) {
        this(orderId, vin, UUID.randomUUID(), Instant.now());
    }
}
