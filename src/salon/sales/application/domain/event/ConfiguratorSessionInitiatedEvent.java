package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

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
