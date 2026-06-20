package salon.billing.infrastructure.out.mock;

import salon.common.application.EventPublisher;
import salon.common.event.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ADAPTER WYJSCIOWY (Rys. 48 — RabbitMq) — atrapa portu {@link EventPublisher} dzialajaca w procesie.
 *
 * Zamiast publikowac na broker, zbiera zdarzenia w pamieci (i wypisuje na konsole). Uzywana w demach
 * i testach jednostkowych do weryfikacji, jakie zdarzenia wyemitowal kontekst.
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
