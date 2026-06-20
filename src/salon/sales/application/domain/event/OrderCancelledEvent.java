package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "ZamowienieAnulowane" — klient zrezygnował z zamówienia (z podanym powodem).
 * Nasłuchują: Kontekst Rozliczeń (rozliczenie zadatku) oraz Inwentarz (zwolnienie blokady).
 */
public record OrderCancelledEvent(UUID eventId,
                                  String orderId,
                                  String reason,
                                  Instant occurredOn) implements DomainEvent {

    public String getReason() {
        return this.reason;
    }
}
