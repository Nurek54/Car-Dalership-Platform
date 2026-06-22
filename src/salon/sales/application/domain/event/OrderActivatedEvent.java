package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-CRM-03 (part 2): the order was activated after the deposit/financing path started.
 * Outbound to Manufacturing/Inventory, which starts the vehicle realization (production line).
 */
public record OrderActivatedEvent(String orderId, UUID eventId, Instant occurredOn) implements DomainEvent {

    public OrderActivatedEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }

    public OrderActivatedEvent(UUID eventId, String orderId, Instant occurredOn) {
        this(orderId, eventId, occurredOn);
    }
}
