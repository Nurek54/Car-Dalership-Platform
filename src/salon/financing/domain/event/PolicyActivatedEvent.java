package salon.financing.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "PolisaWystawiona/Aktywowana" (UC-FIN-02).
public record PolicyActivatedEvent(UUID eventId,
                                   String vin,
                                   Instant occurredOn) implements DomainEvent {
}
