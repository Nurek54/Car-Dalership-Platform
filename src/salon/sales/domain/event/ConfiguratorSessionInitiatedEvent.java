package salon.sales.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "InitiateConfiguratorSession" (UC-CRM-01) — Sprzedaż inicjuje sesję konfiguratora dla klienta.
 * Zdarzenie nasłuchuje Kontekst Katalogu, który otwiera interfejs konfiguratora pojazdów.
 */
public record ConfiguratorSessionInitiatedEvent(UUID eventId,
                                                String sessionId,
                                                String customerId,
                                                String salespersonId,
                                                Instant occurredOn) implements DomainEvent {
}
