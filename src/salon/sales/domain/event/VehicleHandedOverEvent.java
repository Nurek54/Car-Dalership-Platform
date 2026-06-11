package salon.sales.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "PojazdWydany" (UC-CRM-05) — klient odebrał pojazd z salonu.
 * Nasłuchują: Kontekst Rozliczeń (domknięcie salda — handler VehicleHandedOverEventHandler)
 * oraz obsługa posprzedażowa.
 */
public record VehicleHandedOverEvent(UUID eventId,
                                     String orderId,
                                     Instant occurredOn) implements DomainEvent {
}
