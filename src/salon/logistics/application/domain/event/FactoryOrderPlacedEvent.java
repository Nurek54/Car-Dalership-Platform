package salon.logistics.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-INW-02: zlecenie produkcji zostało przyjęte przez fabrykę/importera; nadano VIN,
 * a pojazd oznaczono lokalnie jako "W produkcji".
 */
public record FactoryOrderPlacedEvent(String orderId, String vin,
                                      UUID eventId, Instant occurredOn) implements DomainEvent {

    public FactoryOrderPlacedEvent(String orderId, String vin) {
        this(orderId, vin, UUID.randomUUID(), Instant.now());
    }
}
