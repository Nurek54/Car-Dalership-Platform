package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "OrderCompleted" (UC-CRM-05) — the order reached its final,
 * immutable COMPLETED state (the handover protocol was signed).
 */
public record OrderCompletedEvent(UUID eventId,
                                  String orderId,
                                  Instant occurredOn) implements DomainEvent {
}
