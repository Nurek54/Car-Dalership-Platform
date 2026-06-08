package salon.billing.application.port.in;

import java.math.BigDecimal;

/**
 * Komenda dla UC-FIR-03. transactionId pochodzi z wyciągu bankowego / bramki.
 * gatewayTransactionId jest nullem dla wpłaty rejestrowanej ręcznie przez Księgowego.
 */
public record ProcessPaymentCommand(String orderId,
                                    String transactionId,
                                    BigDecimal amount,
                                    String currency,
                                    String gatewayTransactionId) {

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
            throw new IllegalArgumentException("currency is required.");
        }
    }
}
