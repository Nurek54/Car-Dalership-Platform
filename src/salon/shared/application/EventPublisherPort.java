package salon.shared.application;

import salon.shared.event.DomainEvent;

/**
 * Port wyjściowy (driven port): publikator zdarzeń domenowych.
 * Logika biznesowa zna TYLKO ten kontrakt — nie wie nic o RabbitMQ, JSON-ie ani sieci.
 * Implementacja (adapter) żyje w warstwie infrastruktury.
 */
public interface EventPublisherPort {
    void publish(DomainEvent event);
}
