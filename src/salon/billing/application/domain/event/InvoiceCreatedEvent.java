package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-FIR-02: the final invoice for the order was created and issued. Flows to Sales and CRM.
 *
 * Record of a fact (past perfect tense), consistent with the ubiquitous language of the Billing Context.
 * Carries a minimal set of information (orderId) — sensitive data is fetched by subscribers via ACL.
 */
public record InvoiceCreatedEvent(String orderId, UUID eventId, Instant occurredOn) implements DomainEvent {

    public InvoiceCreatedEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
