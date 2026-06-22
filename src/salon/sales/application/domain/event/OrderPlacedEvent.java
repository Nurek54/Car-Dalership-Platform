package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-CRM-03: an order was created from an accepted offer. Carries the specification id
 * (event-carried state transfer) so downstream contexts (Inventory) can link the order to its
 * specification without querying back.
 */
public record OrderPlacedEvent(String orderId, String offerId, String specificationId,
                               String customerId, UUID eventId, Instant occurredOn) implements DomainEvent {

    public OrderPlacedEvent(String orderId, String offerId, String specificationId, String customerId) {
        this(orderId, offerId, specificationId, customerId, UUID.randomUUID(), Instant.now());
    }

    public OrderPlacedEvent(UUID eventId, String orderId, String specificationId, Instant occurredOn) {
        this(orderId, null, specificationId, null, eventId, occurredOn);
    }
}
