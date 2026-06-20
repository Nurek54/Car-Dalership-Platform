package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-FIR-02 / A1: blad generowania dokumentu (PDF/zapis) — faktura nie powstala.
 */
public record ErrorDuringInvoiceCreationEvent(String orderId, String reason,
                                              UUID eventId, Instant occurredOn) implements DomainEvent {

    public ErrorDuringInvoiceCreationEvent(String orderId, String reason) {
        this(orderId, reason, UUID.randomUUID(), Instant.now());
    }
}
