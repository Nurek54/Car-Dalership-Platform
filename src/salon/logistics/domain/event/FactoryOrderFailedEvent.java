package salon.logistics.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "ZlecenieProdukcjiOdrzucone" (UC-INW-02, scenariusz A1) — API fabryki zwróciło błąd
 * (np. problem z połączeniem); zlecenie produkcji nie zostało przyjęte.
 */
public record FactoryOrderFailedEvent(UUID eventId,
                                      String orderId,
                                      String reason,
                                      Instant occurredOn) implements DomainEvent {
}
