package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-FIR-01: wysłano prośbę o zadatek — klient został poproszony o wpłatę. Płynie do Sprzedaży i CRM (informacja dla Handlowca).
 *
 * Zapis faktu (czas przeszły dokonany), zgodny z językiem wszechobecnym Kontekstu Fakturowania.
 * Niesie minimalny zbiór informacji (orderId) — dane wrażliwe dociągają subskrybenci przez ACL.
 */
public record AdvancePaymentRequestedEvent(String orderId, UUID eventId, Instant occurredOn) implements DomainEvent {

    public AdvancePaymentRequestedEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
