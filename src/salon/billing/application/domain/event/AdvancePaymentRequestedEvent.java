package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-FIR-01: a deposit request was sent — the customer was asked to pay. Flows to Sales and CRM (information for the Salesperson).
 *
 * Record of a fact (past perfect tense), consistent with the ubiquitous language of the Billing Context.
 * Carries a minimal set of information (orderId) — sensitive data is fetched by subscribers via ACL.
 */
public record AdvancePaymentRequestedEvent(String orderId, UUID eventId, Instant occurredOn) implements DomainEvent {

    public AdvancePaymentRequestedEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
