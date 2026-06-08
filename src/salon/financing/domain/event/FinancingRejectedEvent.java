package salon.financing.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "FinansowanieOdrzucone" (UC-FIN-01 A2).
public record FinancingRejectedEvent(UUID eventId,
                                     String orderId,
                                     Instant occurredOn) implements DomainEvent {
}
