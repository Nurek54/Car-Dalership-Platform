package salon.financing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-FIN-02: the bank issued a positive decision. Per the canvas the event goes to the
 * Sales and CRM Context and to Inventory and Logistics (triggering the vehicle reservation).
 */
public record FinancingApprovedEvent(String orderId,
                                     UUID eventId, Instant occurredOn) implements DomainEvent {

    public FinancingApprovedEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
