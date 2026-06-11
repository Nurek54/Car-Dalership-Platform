package salon.shared.application;

import salon.shared.event.DomainEvent;

import java.util.List;

/**
 * Port wyjściowy: publikacja Zdarzeń Dziedziny na magistralę (EventBusAdapter).
 * publishAll pozwala usłudze aplikacyjnej opublikować całą paczkę zdarzeń
 * ściągniętych z agregatu w jednym wywołaniu (po zatwierdzeniu transakcji).
 */
public interface EventPublisherPort {

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
