package salon.billing.infrastructure.in.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Local (ACL) representation of an event from the Sales and CRM Context: a new order ready to
 * initialize the balance. Carries the contract value (event-carried state transfer), so that
 * Billing does not have to query Sales synchronously for the amount.
 *
 * eventId sluzy DEDUPLIKACJI po stronie subskrybenta (idempotencyjnosc).
 */
public record OrderReadyForSettlementEvent(UUID eventId, String orderId,
                                           BigDecimal totalAmount, String currency,
                                           Instant occurredOn) {

    public OrderReadyForSettlementEvent {
        if (eventId == null) {
            throw new IllegalArgumentException("eventId must not be null.");
        }
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        if (totalAmount == null) {
            throw new IllegalArgumentException("totalAmount must not be null.");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency must not be blank.");
        }
    }
}
