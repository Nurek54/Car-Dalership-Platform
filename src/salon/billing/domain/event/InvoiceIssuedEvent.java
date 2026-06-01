package salon.billing.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "FakturaWystawiona" (UC-ROZ-02).
public record InvoiceIssuedEvent(UUID eventId,
                                 String documentId,
                                 String ksefReference,
                                 Instant occurredOn) implements DomainEvent {
}
