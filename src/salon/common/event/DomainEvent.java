package salon.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * A common (marker) interface for all domain events in the system.
 *
 * eventId is used for DEDUPLICATION on the Subscriber side (section 3.4.2) — every event
 * that physically passes through the queue must have a unique identifier.
 */
public interface DomainEvent {
    UUID eventId();
    Instant occurredOn();
}
