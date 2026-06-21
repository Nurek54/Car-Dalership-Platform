package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "OrderActivated" — Sales announces that, after the deposit was posted, fulfillment has started.
public record OrderActivatedEvent(UUID eventId,
                                  String orderId,
                                  Instant occurredOn) implements DomainEvent {
}
