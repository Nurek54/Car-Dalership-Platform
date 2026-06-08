package salon.sales.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "ZamowienieZlozone" (UC-SPR-02) — po podpisaniu umowy zamówienie zostaje formalnie złożone;
// nasłuchuje m.in. Kontekst Inwentarza (alokacja pojazdu/utworzenie slotu produkcyjnego).
public record OrderPlacedEvent(UUID eventId,
                               String orderId,
                               Instant occurredOn) implements DomainEvent {
}
