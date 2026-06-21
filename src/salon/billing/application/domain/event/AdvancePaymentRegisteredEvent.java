package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-FIR-03: the first payment (deposit) after the deposit request has been posted. Flows to Inventory (production order — UC-INW-02).
 *
 * Record of a fact (past perfect tense), consistent with the ubiquitous language of the Billing Context.
 * Carries a minimal set of information (orderId) — sensitive data is fetched by subscribers via ACL.
 */
public record AdvancePaymentRegisteredEvent(String orderId, UUID eventId, Instant occurredOn) implements DomainEvent {

    public AdvancePaymentRegisteredEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
