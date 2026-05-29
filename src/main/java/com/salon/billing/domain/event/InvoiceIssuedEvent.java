package main.java.com.salon.billing.domain.event;

import java.time.Instant;
import java.util.UUID;

// "FakturaWystawiona" (UC-ROZ-02).
public record InvoiceIssuedEvent(UUID documentId,
                                 String ksefReference,
                                 Instant occurredOn) implements DomainEvent {
}