package salon.billing.application.domain.model.settlement;

import salon.common.model.Money;

import java.time.LocalDateTime;

public record Payment(String transactionId, Money amount, LocalDateTime paymentDate) {

    public Payment {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("transactionId must not be blank.");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null.");
        }
        if (amount.isNegative()) {
            throw new IllegalArgumentException("payment amount must not be negative.");
        }
        if (paymentDate == null) {
            throw new IllegalArgumentException("paymentDate must not be null.");
        }
    }
}
