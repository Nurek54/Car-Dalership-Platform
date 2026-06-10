package salon.logistics.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "BladWydaniaPojazdu" (UC-INW-06, A1) — próba wydania pojazdu w niewłaściwym stanie.
public record VehicleInventoryReleasedError(UUID eventId,
                                            String vin,
                                            String reason,
                                            Instant occurredOn) implements DomainEvent {
}
