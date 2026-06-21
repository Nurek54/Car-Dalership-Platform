package salon.logistics.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-INW-03: the physical vehicle came off the transporter onto the yard and was matched with the order
 * (status "Zarezerwowany").
 */
public record VehicleDeliveredToStockEvent(String orderId, String vin,
                                           UUID eventId, Instant occurredOn) implements DomainEvent {

    public VehicleDeliveredToStockEvent(String orderId, String vin) {
        this(orderId, vin, UUID.randomUUID(), Instant.now());
    }
}
