package main.java.com.salon.billing.application.port.out;

import main.java.com.salon.billing.domain.event.DomainEvent;

// Publikator zdarzeń domenowych.
public interface EventPublisherPort {
    void publish(DomainEvent event);
}