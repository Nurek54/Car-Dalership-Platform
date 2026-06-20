package salon.financing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-FIN-02: bank wydał decyzję pozytywną. Zgodnie z kanwą zdarzenie trafia do Kontekstu
 * Sprzedaży i CRM oraz do Inwentarza i Logistyki (uruchomienie rezerwacji pojazdu).
 */
public record FinancingApprovedEvent(String orderId,
                                     UUID eventId, Instant occurredOn) implements DomainEvent {

    public FinancingApprovedEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
