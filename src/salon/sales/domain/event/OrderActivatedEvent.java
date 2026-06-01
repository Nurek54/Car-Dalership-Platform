package salon.sales.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "ZamowienieAktywowane" — Sprzedaż ogłasza, że po zaksięgowaniu zadatku ruszyła realizacja.
public record OrderActivatedEvent(UUID eventId,
                                  String orderId,
                                  Instant occurredOn) implements DomainEvent {
}
