package main.java.com.salon.billing.domain.event;

import java.time.Instant;
import java.util.UUID;

// "ZadatekZaksiegowany" (UC-ROZ-01) — odblokowuje realizację zamówienia.
public record DepositRegisteredEvent(UUID paymentId,
                                     String orderId,
                                     Instant occurredOn) implements DomainEvent {
}
