package main.java.com.salon.billing.infrastructure.mock;

import main.java.com.salon.billing.application.port.out.EventPublisherPort;
import main.java.com.salon.billing.domain.event.DomainEvent;

/**
 * Publikator zdarzeń (EventBusAdapter) — wersja MOCK (sekcja 3.4.2 dokumentacji).
 *
 * W realnym systemie ten adapter serializuje zdarzenie do formatu tekstowego (JSON)
 * i wysyła je do brokera (np. RabbitMQ) lub zapisuje w Event Store.
 * Tutaj tylko serializujemy do prostego JSON-a i wypisujemy na konsolę.
 *
 * Deduplikacja (idempotencyjność) jest odpowiedzialnością SUBSKRYBENTA, nie tego adaptera.
 */
public class EventBusAdapter implements EventPublisherPort {

    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        String json = toJson(event);
        System.out.println("[EventBusAdapter] Publishing event: " + json);
        // TODO: tutaj realna wysyłka do brokera (RabbitMQ) lub zapis do Event Store.
    }

    // Prosta, ręczna serializacja do JSON (mock, bez bibliotek zewnętrznych).
    // W realnym adapterze użylibyśmy np. Jacksona, by zserializować wszystkie pola zdarzenia.
    private String toJson(DomainEvent event) {
        return "{"
                + "\"type\":\"" + event.getClass().getSimpleName() + "\","
                + "\"occurredOn\":\"" + event.occurredOn() + "\""
                + "}";
    }
}
