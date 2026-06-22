package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-CRM-04: the order met all requirements and is ready for handover. Internal notification that
 * triggers a salesperson alert to schedule the pickup with the customer.
 */
public record OrderReadyForHandoverEvent(String orderId, UUID eventId, Instant occurredOn) implements DomainEvent {

    public OrderReadyForHandoverEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }

    public OrderReadyForHandoverEvent(UUID eventId, String orderId, Instant occurredOn) {
        this(orderId, eventId, occurredOn);
    }
}
