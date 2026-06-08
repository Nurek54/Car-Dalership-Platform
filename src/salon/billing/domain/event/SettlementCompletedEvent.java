package salon.billing.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** "RozliczenieZakonczone" (UC-FIR-03) — należność pokryta w całości (saldo zerowe). */
public record SettlementCompletedEvent(UUID eventId,
                                       String settlementId,
                                       String orderId,
                                       Instant occurredOn) implements DomainEvent {
}
