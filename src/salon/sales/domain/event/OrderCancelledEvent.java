package salon.sales.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "ZamowienieAnulowane" (UC-SPR-03) — zamówienie zostało anulowane wraz z powodem.
 * Powód (reason) niesie informację dla Rozliczeń, jak potraktować zadatek.
 *
 * Uwaga: oprócz akcesora rekordu reason() udostępniamy też getReason() w stylu JavaBean,
 * bo testy odwołują się do event.getReason().
 */
public record OrderCancelledEvent(UUID eventId,
                                  String orderId,
                                  String reason,
                                  Instant occurredOn) implements DomainEvent {

    public String getReason() {
        return this.reason;
    }
}
