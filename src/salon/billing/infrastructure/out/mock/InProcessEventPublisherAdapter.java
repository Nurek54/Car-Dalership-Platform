package salon.billing.infrastructure.out.mock;

import salon.common.application.EventPublisher;
import salon.common.event.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OUTBOUND ADAPTER (Fig. 48 — RabbitMq) — a mock of the {@link EventPublisher} port running in-process.
 *
 * Instead of publishing to a broker, it collects events in memory (and prints to the console). Used in demos
 * and unit tests to verify which events the context emitted.
 */
public class InProcessEventPublisherAdapter implements EventPublisher {

    private final List<DomainEvent> publishedEvents = new ArrayList<>();

    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        this.publishedEvents.add(event);
        System.out.println("[InProcessEventPublisherAdapter] -> " + event.getClass().getSimpleName());
    }

    public List<DomainEvent> publishedEvents() {
        return Collections.unmodifiableList(new ArrayList<>(this.publishedEvents));
    }
}
