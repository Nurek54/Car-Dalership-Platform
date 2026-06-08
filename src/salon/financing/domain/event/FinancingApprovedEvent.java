package salon.financing.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "FinansowanieZatwierdzone" (UC-FIN-01) — nasłuchuje Kontekst Rozliczeń (UC-ROZ-03).
public record FinancingApprovedEvent(UUID eventId,
                                     String orderId,
                                     Instant occurredOn) implements DomainEvent {
}
