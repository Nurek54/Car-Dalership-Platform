package salon.catalog.application.port.out;

import salon.catalog.application.domain.model.event.DomainEvent;

/**
 * PORT WYJŚCIOWY – publikacja zdarzeń dziedziny na zewnątrz kontekstu
 * („EventPublisher” → RabbitMQ). Realizuje komunikację między odległymi kontekstami
 * (metoda push przez middleware) oraz spójność ostateczną (Reguła 4 agregatów).
 */
public interface EventPublisher {

    void publish(DomainEvent event);
}
