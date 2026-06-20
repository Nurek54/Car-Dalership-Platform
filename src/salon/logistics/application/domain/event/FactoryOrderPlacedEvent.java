package salon.logistics.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "ZlecenieProdukcjiZlozone" (UC-INW-02) — zlecenie produkcji wysłane do API
 * Importera/Fabryki, zamówienie oznaczone lokalnie statusem "W produkcji".
 */
public record FactoryOrderPlacedEvent(UUID eventId,
                                      String orderId,
                                      String vin,
                                      Instant occurredOn) implements DomainEvent {
}
