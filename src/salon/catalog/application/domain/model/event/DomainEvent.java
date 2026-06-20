package salon.catalog.application.domain.model.event;

import java.time.Instant;

/**
 * Zdarzenie dziedziny – komunikat o zmianie lub przetworzeniu elementów modelu
 * dziedziny (wynik wykonania operacji agregatu). Nazwa w czasie przeszłym dokonanym.
 *
 * Zdarzenia są NIEMUTOWALNE; zawierają znacznik czasu (occurredOn), identyfikator
 * agregatu publikującego oraz minimalny zbiór informacji.
 */
public interface DomainEvent {

    /** Znacznik czasu wystąpienia zdarzenia. */
    Instant occurredOn();

    /** Nazwa zdarzenia (typ) – używana m.in. do deduplikacji u subskrybenta. */
    String eventName();
}
