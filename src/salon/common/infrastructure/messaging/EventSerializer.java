package salon.common.infrastructure.messaging;

import salon.common.event.DomainEvent;

public interface EventSerializer {
    String toJson(DomainEvent event);
}
