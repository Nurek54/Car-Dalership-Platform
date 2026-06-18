package salon.catalog.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "CatalogVersionPublished" (UC-KON-02) — opublikowano NOWĄ aktywną wersję cennika.
 *
 * W odróżnieniu od {@code CatalogUpdatedEvent} (sygnał skierowany "w świat", do innych
 * kontekstów: Sprzedaż, Logistyka) to zdarzenie jest WEWNĘTRZNYM wyzwalaczem domeny Katalogu:
 * nasłuchuje go handler, który archiwizuje POPRZEDNI cennik ({@code previousCatalogId})
 * w OSOBNEJ transakcji (eventual consistency). Dzięki temu w jednej transakcji modyfikujemy
 * tylko jeden Agregat (złota zasada DDD — granice spójności).
 *
 * {@code previousCatalogId} może być null/blank, jeśli to pierwsza wersja cennika dla rocznika.
 */
public record CatalogVersionPublishedEvent(UUID eventId,
                                           String newCatalogId,
                                           String modelYear,
                                           String previousCatalogId,
                                           Instant occurredOn) implements DomainEvent {
}
