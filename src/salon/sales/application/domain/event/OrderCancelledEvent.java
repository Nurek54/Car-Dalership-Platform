package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "OrderCancelled" — the customer cancelled the order (with a given reason).
 * Listeners: the Billing Context (deposit settlement) and Inventory (lock release).
 */
public record OrderCancelledEvent(UUID eventId,
                                  String orderId,
                                  String reason,
                                  Instant occurredOn) implements DomainEvent {

    public String reason() {
        return this.reason;
    }
}
