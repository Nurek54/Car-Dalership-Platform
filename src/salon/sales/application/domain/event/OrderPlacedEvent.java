package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record OrderPlacedEvent(String orderId, String offerId, String specificationId,
                               String customerId, UUID eventId, Instant occurredOn) implements DomainEvent {

    public OrderPlacedEvent(String orderId, String offerId, String specificationId, String customerId) {
        this(orderId, offerId, specificationId, customerId, UUID.randomUUID(), Instant.now());
    }

    public OrderPlacedEvent(UUID eventId, String orderId, String specificationId, Instant occurredOn) {
        this(orderId, null, specificationId, null, eventId, occurredOn);
    }
}
