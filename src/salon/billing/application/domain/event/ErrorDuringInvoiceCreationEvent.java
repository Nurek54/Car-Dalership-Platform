package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-FIR-02 / A1: document generation error (PDF/persistence) — the invoice was not created.
 */
public record ErrorDuringInvoiceCreationEvent(String orderId, String reason,
                                              UUID eventId, Instant occurredOn) implements DomainEvent {

    public ErrorDuringInvoiceCreationEvent(String orderId, String reason) {
        this(orderId, reason, UUID.randomUUID(), Instant.now());
    }
}
