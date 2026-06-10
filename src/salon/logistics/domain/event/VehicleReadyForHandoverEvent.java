package salon.logistics.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "PojazdGotowyDoWydania" (UC-INW-05) — saldo rozliczone, auto czeka na odbiór klienta.
public record VehicleReadyForHandoverEvent(UUID eventId,
                                           String orderId,
                                           String vin,
                                           Instant occurredOn) implements DomainEvent {
}
