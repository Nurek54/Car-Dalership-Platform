package main.java.com.salon.billing.domain.event;

import java.time.Instant;

// Wspólny interfejs (marker) dla wszystkich zdarzeń domenowych.
public interface DomainEvent {
    Instant occurredOn();
}