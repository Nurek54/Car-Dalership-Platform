package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "FinancingRequested" (UC-CRM-03, step 5) — an integration message for
 * the Financing Context (UC-FIN-01): triggers the creditworthiness check
 * through the bank ACL (the FinancingRequestedEventHandler).
 */
public record FinancingRequestedEvent(UUID eventId,
                                      String orderId,
                                      String customerId,
                                      Instant occurredOn) implements DomainEvent {
}
