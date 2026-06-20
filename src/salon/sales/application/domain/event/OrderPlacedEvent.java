package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "ZamowienieZlozone" (UC-CRM-03) — zamówienie powstało z zaakceptowanej oferty.
 * Nasłuchują: Kontekst Rozliczeń (inicjalizacja salda) oraz Inwentarz
 * (alokacja pojazdu / slotu produkcyjnego — handler OrderPlacedEventHandler).
 *
 * Zdarzenie niesie specificationId (event-carried state transfer): Inwentarz wiąże
 * zamówienie ze specyfikacją lokalnie i nie musi synchronicznie odpytywać
 * Kontekstu Sprzedaży ani Katalogu o kody wyposażenia (UC-INW-01/02).
 */
public record OrderPlacedEvent(UUID eventId,
                               String orderId,
                               String specificationId,
                               Instant occurredOn) implements DomainEvent {
}
