package main.java.com.salon.billing.domain.event;

import java.time.Instant;
import java.util.UUID;

// UC-ROZ-01, A2: niedopłata zaksięgowana jako zaliczka -> powiadomienie Handlowca.
public record AdvanceRegisteredEvent(UUID paymentId,
                                     String orderId,
                                     Instant occurredOn) implements DomainEvent {
}
