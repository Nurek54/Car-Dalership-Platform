package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-FIR-03: order balance = 0 PLN (fully paid).
 * Flows to Inventory and Logistics — vehicle "Ready for handover" (UC-INW-05).
 *
 * Record of a fact (past perfect tense), consistent with the ubiquitous language of the Billing Context.
 */
public record SettlementCompletedEvent(String orderId, UUID eventId, Instant occurredOn)
        implements DomainEvent {

    public SettlementCompletedEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
