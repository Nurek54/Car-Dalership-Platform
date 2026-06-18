package salon.catalog.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "CatalogUpdateFailed" (UC-KON-02, scenariusz A1) — błąd translacji (ACL) lub walidacji
// pakietu danych od Importera. Aktualizacja jest przerywana, a sygnał o błędzie integracji
// trafia w świat (logowanie / wsparcie IT). 'reason' niesie powód odrzucenia pakietu.
public record CatalogUpdateFailedEvent(UUID eventId,
                                       String modelYear,
                                       String reason,
                                       Instant occurredOn) implements DomainEvent {
}
