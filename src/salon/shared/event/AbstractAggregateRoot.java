package salon.shared.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Wspólna baza dla Aggregate Rootów, które emitują zdarzenia domenowe.
 *
 * Wzorzec "collect & pull": agregat na podstawie SWOJEGO stanu rejestruje zdarzenia
 * (registerEvent), a warstwa aplikacji po wykonaniu operacji ściąga je (pullDomainEvents)
 * i publikuje przez port. Dzięki temu decyzja "jakie zdarzenie" zostaje w domenie,
 * a serwis aplikacyjny nie zagląda do wewnętrznego stanu agregatu.
 */
public abstract class AbstractAggregateRoot {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // Wołane wyłącznie z wnętrza agregatu (metody biznesowe).
    protected void registerEvent(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        this.domainEvents.add(event);
    }

    /**
     * Zwraca zebrane zdarzenia i CZYŚCI listę (jednorazowe ściągnięcie).
     * Kopia obronna — wołający nie modyfikuje wewnętrznej listy.
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> copy = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return Collections.unmodifiableList(copy);
    }
}
