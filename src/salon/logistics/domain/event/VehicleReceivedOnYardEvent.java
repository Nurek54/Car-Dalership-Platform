package salon.logistics.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "PojazdPrzyjetyNaPlac" (UC-INW-01) — fizyczny egzemplarz zjechał na plac salonu.
public record VehicleReceivedOnYardEvent(UUID eventId,
                                         String vin,
                                         Instant occurredOn) implements DomainEvent {
}
