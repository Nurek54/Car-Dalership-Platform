package salon.common.infrastructure.messaging;

import salon.common.event.DomainEvent;

// Contract for serializing an event to text (JSON). The publishing adapter depends only on this.
public interface EventSerializer {
    String toJson(DomainEvent event);
}
