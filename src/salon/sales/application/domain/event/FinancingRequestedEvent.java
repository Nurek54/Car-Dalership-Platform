package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "ZawnioskowanoOFinansowanie" (UC-CRM-03, krok 5) — komunikat integracyjny dla
 * Kontekstu Finansowania (UC-FIN-01): uruchamia weryfikację zdolności kredytowej
 * przez ACL banku (handler FinancingRequestedEventHandler).
 */
public record FinancingRequestedEvent(UUID eventId,
                                      String orderId,
                                      String customerId,
                                      Instant occurredOn) implements DomainEvent {
}
