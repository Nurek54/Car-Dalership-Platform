package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "OrderReadyForHandover" (UC-CRM-04) — the vehicle is ready physically and financially.
 * A signal to generate a notification for the Salesperson, who schedules the pickup date with the customer.
 */
public record OrderReadyForHandoverEvent(UUID eventId,
                                         String orderId,
                                         Instant occurredOn) implements DomainEvent {
}
