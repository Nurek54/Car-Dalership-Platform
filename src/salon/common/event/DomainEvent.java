package salon.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Wspólny interfejs (marker) dla wszystkich zdarzeń domenowych w systemie.
 *
 * eventId służy do DEDUPLIKACJI po stronie Subskrybenta (sekcja 3.4.2) — każde zdarzenie,
 * które fizycznie przechodzi przez kolejkę, musi mieć unikalny identyfikator.
 */
public interface DomainEvent {
    UUID eventId();
    Instant occurredOn();
}
