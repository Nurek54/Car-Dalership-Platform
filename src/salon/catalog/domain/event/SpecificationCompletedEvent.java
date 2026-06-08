package salon.catalog.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "SpecyfikacjaSkompletowana" (UC-KAT-01) — konfiguracja gotowa do sprzedaży;
// to zdarzenie odblokowuje przygotowanie oferty w Kontekście Sprzedaży.
public record SpecificationCompletedEvent(UUID eventId,
                                          String specificationId,
                                          String catalogId,
                                          Instant occurredOn) implements DomainEvent {
}
