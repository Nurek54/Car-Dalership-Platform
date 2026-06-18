package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "ZamowienieZrealizowane" (UC-CRM-05) — zamówienie osiągnęło końcowy,
 * niemutowalny stan COMPLETED (protokół wydania podpisany).
 */
public record OrderCompletedEvent(UUID eventId,
                                  String orderId,
                                  Instant occurredOn) implements DomainEvent {
}
