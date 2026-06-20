package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-FIR-02: utworzono i wystawiono fakturę końcową dla zamówienia. Płynie do Sprzedaży i CRM.
 *
 * Zapis faktu (czas przeszły dokonany), zgodny z językiem wszechobecnym Kontekstu Fakturowania.
 * Niesie minimalny zbiór informacji (orderId) — dane wrażliwe dociągają subskrybenci przez ACL.
 */
public record InvoiceCreatedEvent(String orderId, UUID eventId, Instant occurredOn) implements DomainEvent {

    public InvoiceCreatedEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
