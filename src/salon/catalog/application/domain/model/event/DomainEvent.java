package salon.catalog.application.domain.model.event;

import java.time.Instant;

public interface DomainEvent {

    Instant occurredOn();

    String eventName();
}
