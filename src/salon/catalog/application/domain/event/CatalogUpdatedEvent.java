package salon.catalog.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "CatalogUpdated" (UC-KON-02) — zapis nowej wersji cennika rozsyła sygnał w świat;
// nasłuchuje m.in. Kontekst Sprzedaży, by unieważnić oferty oparte o starsze cenniki.
public record CatalogUpdatedEvent(UUID eventId,
                                  String catalogId,
                                  String modelYear,
                                  Instant occurredOn) implements DomainEvent {
}
