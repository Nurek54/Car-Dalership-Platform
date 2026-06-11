package salon.sales.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "ZamowienieZlozone" (UC-CRM-03) — zamówienie powstało z zaakceptowanej oferty.
 * Nasłuchują: Kontekst Rozliczeń (inicjalizacja salda) oraz Inwentarz
 * (alokacja pojazdu / slotu produkcyjnego — handler OrderPlacedEventHandler).
 */
public record OrderPlacedEvent(UUID eventId,
                               String orderId,
                               Instant occurredOn) implements DomainEvent {
}
