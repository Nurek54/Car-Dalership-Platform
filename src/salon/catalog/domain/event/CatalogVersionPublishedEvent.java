package salon.catalog.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "NowaWersjaCennikaOpublikowana" (UC-KAT-02) — aktywacja cennika rozsyła sygnał w świat;
// nasłuchuje m.in. Kontekst Sprzedaży, by unieważnić oferty oparte o starsze cenniki.
public record CatalogVersionPublishedEvent(UUID eventId,
                                           String catalogId,
                                           String modelYear,
                                           Instant occurredOn) implements DomainEvent {
}
