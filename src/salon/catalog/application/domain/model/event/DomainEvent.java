package salon.catalog.application.domain.model.event;

import java.time.Instant;

/**
 * Domain event – a message about a change or processing of domain model
 * elements (the result of an aggregate operation). The name is in the past perfect tense.
 *
 * Events are IMMUTABLE; they contain a timestamp (occurredOn), the identifier
 * of the publishing aggregate and a minimal set of information.
 */
public interface DomainEvent {

    /** Timestamp of the event's occurrence. */
    Instant occurredOn();

    /** Event name (type) – used, among other things, for deduplication at the subscriber. */
    String eventName();
}
