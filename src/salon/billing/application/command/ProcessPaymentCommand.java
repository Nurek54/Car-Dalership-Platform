package salon.billing.application.command;

import java.math.BigDecimal;

/**
 * Model danych wejsciowych portu ProcessPayment (UC-FIR-03) — sparowany przelew z wyciagu.
 */
public record ProcessPaymentCommand(String orderId, String transactionId,
                                    BigDecimal amount, String currency) {

    public ProcessPaymentCommand {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("transactionId must not be blank.");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null.");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency must not be blank.");
        }
    }
}
