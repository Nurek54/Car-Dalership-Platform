package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-CRM-02: a proforma offer was created and presented to the customer (internal notification).
 */
public record OfferCreatedEvent(String offerId, String customerId, String specificationId,
                                UUID eventId, Instant occurredOn) implements DomainEvent {

    public OfferCreatedEvent(String offerId, String customerId, String specificationId) {
        this(offerId, customerId, specificationId, UUID.randomUUID(), Instant.now());
    }
}
