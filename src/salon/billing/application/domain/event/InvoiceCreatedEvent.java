package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** "FakturaUtworzona" (UC-FIR-02) — wystawiono prawnie wiążący dokument księgowy. */
public record InvoiceCreatedEvent(UUID eventId,
                                  String documentId,
                                  String orderId,
                                  Instant occurredOn) implements DomainEvent {
}
