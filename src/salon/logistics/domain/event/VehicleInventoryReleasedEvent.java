package salon.logistics.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "PojazdWydanyZInwentarza" (UC-INW-06) — pojazd przekazany klientowi (HANDED_OVER).
public record VehicleInventoryReleasedEvent(UUID eventId,
                                            String orderId,
                                            String vin,
                                            Instant occurredOn) implements DomainEvent {
}
