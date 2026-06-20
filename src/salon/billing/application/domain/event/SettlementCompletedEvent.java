package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-FIR-03: saldo zamowienia = 0 PLN (w pelni oplacone).
 * Plynie do Inwentarza i Logistyki — pojazd "Gotowy do wydania" (UC-INW-05).
 *
 * Zapis faktu (czas przeszly dokonany), zgodny z jezykiem wszechobecnym Kontekstu Fakturowania.
 */
public record SettlementCompletedEvent(String orderId, UUID eventId, Instant occurredOn)
        implements DomainEvent {

    public SettlementCompletedEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
