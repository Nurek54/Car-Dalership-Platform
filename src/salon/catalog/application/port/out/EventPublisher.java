package salon.catalog.application.port.out;

import salon.catalog.application.domain.model.event.DomainEvent;

public interface EventPublisher {

    void publish(DomainEvent event);
}
