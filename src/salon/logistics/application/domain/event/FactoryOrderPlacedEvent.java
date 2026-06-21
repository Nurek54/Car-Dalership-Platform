package salon.logistics.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-INW-02: the production order was accepted by the factory/importer; a VIN was assigned,
 * and the vehicle was marked locally as "In production".
 */
public record FactoryOrderPlacedEvent(String orderId, String vin,
                                      UUID eventId, Instant occurredOn) implements DomainEvent {

    public FactoryOrderPlacedEvent(String orderId, String vin) {
        this(orderId, vin, UUID.randomUUID(), Instant.now());
    }
}
