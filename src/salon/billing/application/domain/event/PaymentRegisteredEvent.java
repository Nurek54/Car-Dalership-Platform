package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-FIR-03: a bank payment for the order was posted. Flows to Sales and CRM (order activation — UC-CRM-03 part 2).
 *
 * Record of a fact (past perfect tense), consistent with the ubiquitous language of the Billing Context.
 * Carries a minimal set of information (orderId) — sensitive data is fetched by subscribers via ACL.
 */
public record PaymentRegisteredEvent(String orderId, UUID eventId, Instant occurredOn) implements DomainEvent {

    public PaymentRegisteredEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
