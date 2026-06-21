package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "OrderPlaced" (UC-CRM-03) — the order was created from an accepted offer.
 * Listeners: the Billing Context (balance initialization) and Inventory
 * (vehicle / production-slot allocation — the OrderPlacedEventHandler).
 *
 * The event carries specificationId (event-carried state transfer): Inventory links
 * the order with the specification locally and does not have to synchronously query
 * the Sales Context or the Catalog for the equipment codes (UC-INW-01/02).
 */
public record OrderPlacedEvent(UUID eventId,
                               String orderId,
                               String specificationId,
                               Instant occurredOn) implements DomainEvent {
}
