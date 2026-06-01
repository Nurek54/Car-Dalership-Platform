package salon.billing.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "ZadatekZaksiegowany" (UC-ROZ-01) — odblokowuje realizację zamówienia w Sprzedaży.
public record DepositRegisteredEvent(UUID eventId,
                                     String paymentId,
                                     String orderId,
                                     Instant occurredOn) implements DomainEvent {
}
