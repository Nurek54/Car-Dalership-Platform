package salon.common.application;

import salon.common.event.DomainEvent;

import java.util.List;

/**
 * Outbound port: publishing Domain Events to the bus (EventBusAdapter).
 * publishAll lets the application service publish an entire batch of events
 * pulled from the aggregate in a single call (after the transaction commits).
 */
public interface EventPublisher {

    void publish(DomainEvent event);

    default void publishAll(List<DomainEvent> events) {
        if (events == null) {
            return;
        }
        for (DomainEvent event : events) {
            publish(event);
        }
    }
}
