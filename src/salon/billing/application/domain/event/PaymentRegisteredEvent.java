package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-FIR-03: zaksięgowano wpłatę bankową dla zamówienia. Płynie do Sprzedaży i CRM (aktywacja zamówienia — UC-CRM-03 cz.2).
 *
 * Zapis faktu (czas przeszły dokonany), zgodny z językiem wszechobecnym Kontekstu Fakturowania.
 * Niesie minimalny zbiór informacji (orderId) — dane wrażliwe dociągają subskrybenci przez ACL.
 */
public record PaymentRegisteredEvent(String orderId, UUID eventId, Instant occurredOn) implements DomainEvent {

    public PaymentRegisteredEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
