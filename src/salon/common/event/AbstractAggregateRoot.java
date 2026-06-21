package salon.common.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A common base for Aggregate Roots that emit domain events.
 *
 * The "collect & pull" pattern: the aggregate records events based on ITS OWN state
 * (registerEvent), and the application layer pulls them after the operation (pullDomainEvents)
 * and publishes them through the port. Thanks to this the decision "which event" stays in the domain,
 * and the application service does not peek into the aggregate's internal state.
 */
public abstract class AbstractAggregateRoot {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // Called only from inside the aggregate (business methods).
    protected void registerEvent(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        this.domainEvents.add(event);
    }

    /**
     * Returns the collected events and CLEARS the list (a one-time pull).
     * Defensive copy — the caller does not modify the internal list.
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> copy = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return Collections.unmodifiableList(copy);
    }

    /**
     * Preview of the registered events WITHOUT clearing the list.
     * Unlike pullDomainEvents() it does not "consume" the events — it serves unit tests
     * that assert which events the aggregate emitted (getDomainEvents()).
     * We return an unmodifiable defensive copy, so the test will not corrupt the internal list.
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(new ArrayList<>(this.domainEvents));
    }
}
