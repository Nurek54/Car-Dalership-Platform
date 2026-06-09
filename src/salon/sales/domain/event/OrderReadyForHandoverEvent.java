package salon.sales.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "ZamowienieGotoweDoOdbioru" (UC-CRM-04) — pojazd jest gotowy fizycznie i finansowo.
 * Sygnał do wygenerowania powiadomienia dla Handlowca, który umawia termin odbioru z klientem.
 */
public record OrderReadyForHandoverEvent(UUID eventId,
                                         String orderId,
                                         Instant occurredOn) implements DomainEvent {
}
