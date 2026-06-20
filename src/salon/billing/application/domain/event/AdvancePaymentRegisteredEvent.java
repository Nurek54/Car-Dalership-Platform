package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-FIR-03: pierwsza wpłata (zadatek) po prośbie o zadatek została zaksięgowana. Płynie do Inwentarza (zlecenie produkcji — UC-INW-02).
 *
 * Zapis faktu (czas przeszły dokonany), zgodny z językiem wszechobecnym Kontekstu Fakturowania.
 * Niesie minimalny zbiór informacji (orderId) — dane wrażliwe dociągają subskrybenci przez ACL.
 */
public record AdvancePaymentRegisteredEvent(String orderId, UUID eventId, Instant occurredOn) implements DomainEvent {

    public AdvancePaymentRegisteredEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
