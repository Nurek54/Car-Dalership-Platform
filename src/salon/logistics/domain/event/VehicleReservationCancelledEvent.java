package salon.logistics.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "RezerwacjaPojazduAnulowana" (UC-INW-04) — zwolnienie blokady po wygaśnięciu terminu płatności.
public record VehicleReservationCancelledEvent(UUID eventId,
                                               String orderId,
                                               String vin,
                                               Instant occurredOn) implements DomainEvent {
}
