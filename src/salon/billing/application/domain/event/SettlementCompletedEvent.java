package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** "RozliczenieZakonczone" (UC-FIR-03) — należność pokryta w całości (saldo zerowe). */
public record SettlementCompletedEvent(UUID eventId,
                                       String settlementId,
                                       String orderId,
                                       Instant occurredOn) implements DomainEvent {
}
