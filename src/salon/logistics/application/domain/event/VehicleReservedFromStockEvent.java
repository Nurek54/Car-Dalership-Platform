package salon.logistics.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-INW-01: wolny pojazd z placu został twardo zarezerwowany (blokada na VIN) dla zamówienia.
 */
public record VehicleReservedFromStockEvent(String orderId, String vin,
                                            UUID eventId, Instant occurredOn) implements DomainEvent {

    public VehicleReservedFromStockEvent(String orderId, String vin) {
        this(orderId, vin, UUID.randomUUID(), Instant.now());
    }
}
