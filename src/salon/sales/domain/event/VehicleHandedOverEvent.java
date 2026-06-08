package salon.sales.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "PojazdWydany" (UC-SPR-08) — finalne wydanie auta klientowi kończy zamówienie;
// sygnał m.in. dla Rozliczeń (domknięcie salda) i obsługi posprzedażowej.
public record VehicleHandedOverEvent(UUID eventId,
                                     String orderId,
                                     Instant occurredOn) implements DomainEvent {
}
