package salon.billing.infrastructure.out.mock;

import salon.common.application.EventPublisher;
import salon.common.event.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
