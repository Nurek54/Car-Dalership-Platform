package salon.logistics.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-INW-03: fizyczny pojazd zjechał z lawety na plac i został sparowany z zamówieniem
 * (status "Zarezerwowany").
 */
public record VehicleDeliveredToStockEvent(String orderId, String vin,
                                           UUID eventId, Instant occurredOn) implements DomainEvent {

    public VehicleDeliveredToStockEvent(String orderId, String vin) {
        this(orderId, vin, UUID.randomUUID(), Instant.now());
    }
}
