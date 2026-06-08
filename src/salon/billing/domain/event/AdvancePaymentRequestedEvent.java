package salon.billing.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** "ZadatekZazadany" (UC-FIR-01) — agregat Settlement zażądał wpłaty zadatku. */
public record AdvancePaymentRequestedEvent(UUID eventId,
                                           String settlementId,
                                           String orderId,
                                           Instant occurredOn) implements DomainEvent {
}
