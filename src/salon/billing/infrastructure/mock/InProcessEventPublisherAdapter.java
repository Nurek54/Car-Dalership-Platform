package salon.billing.infrastructure.mock;

import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;

/**
 * Adapter OFFLINE: publikuje zdarzenia "w procesie" (tylko log na konsolę), bez brokera.
 * Używamy go w testach i w trybie demonstracji bez RabbitMQ. W produkcji podmieniamy
 * go na RabbitMqEventPublisherAdapter — reszta kodu się nie zmienia (ten sam port).
 */
public class InProcessEventPublisherAdapter implements EventPublisherPort {

    @Override
    public void publish(DomainEvent event) {
        System.out.println("[InProcessEventPublisher] " + event.getClass().getSimpleName()
                + " eventId=" + event.eventId());
    }
}
