package salon.logistics.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-INW-06 / A1: komenda wydania odrzucona, bo pojazd nie był w stanie "Gotowy do wydania".
 * Zdarzenie kompensacyjne dla Kontekstu Sprzedaży (powrót zamówienia do READY_FOR_HANDOVER).
 */
public record VehicleInventoryReleasedErrorEvent(String orderId, String reason,
                                                 UUID eventId, Instant occurredOn) implements DomainEvent {

    public VehicleInventoryReleasedErrorEvent(String orderId, String reason) {
        this(orderId, reason, UUID.randomUUID(), Instant.now());
    }
}
