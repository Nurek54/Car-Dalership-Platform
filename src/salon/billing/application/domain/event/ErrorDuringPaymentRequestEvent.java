package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-FIR-01 / A1: missing information required to generate the deposit request.
 */
public record ErrorDuringPaymentRequestEvent(String orderId, String reason,
                                             UUID eventId, Instant occurredOn) implements DomainEvent {

    public ErrorDuringPaymentRequestEvent(String orderId, String reason) {
        this(orderId, reason, UUID.randomUUID(), Instant.now());
    }
}
