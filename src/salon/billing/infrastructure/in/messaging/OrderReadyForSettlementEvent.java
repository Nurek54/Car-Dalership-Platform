package salon.billing.infrastructure.in.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

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
