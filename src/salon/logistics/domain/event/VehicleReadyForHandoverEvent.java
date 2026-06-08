package salon.logistics.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "PojazdGotowyDoWydania" (UC-INW-02) — PDI zatwierdzone, auto gotowe do wydania.
public record VehicleReadyForHandoverEvent(UUID eventId,
                                           String vin,
                                           Instant occurredOn) implements DomainEvent {
}
