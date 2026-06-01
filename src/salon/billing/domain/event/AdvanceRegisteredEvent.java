package salon.billing.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// UC-ROZ-01, A2: niedopłata zaksięgowana jako zaliczka -> powiadomienie Handlowca.
public record AdvanceRegisteredEvent(UUID eventId,
                                     String paymentId,
                                     String orderId,
                                     Instant occurredOn) implements DomainEvent {
}
