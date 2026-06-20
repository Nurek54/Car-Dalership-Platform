package salon.financing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-FIN-01 / A2: bank natychmiast odrzucił wniosek z powodu błędnych/niekompletnych danych
 * (np. błędny NIP). Zdarzenie wraca do Kontekstu Sprzedaży i CRM, aby handlowiec poprawił wniosek.
 */
public record FinancingApplicationFailedEvent(String orderId, String reason,
                                              UUID eventId, Instant occurredOn) implements DomainEvent {

    public FinancingApplicationFailedEvent(String orderId, String reason) {
        this(orderId, reason, UUID.randomUUID(), Instant.now());
    }
}
