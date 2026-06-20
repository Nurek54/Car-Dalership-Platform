package salon.financing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-FIN-02 / A1: bank wydał decyzję negatywną. Zdarzenie trafia do Kontekstu Sprzedaży i CRM.
 */
public record FinancingRejectedEvent(String orderId,
                                     UUID eventId, Instant occurredOn) implements DomainEvent {

    public FinancingRejectedEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
