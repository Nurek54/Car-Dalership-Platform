package salon.billing.application.command;

import java.math.BigDecimal;

/**
 * Komenda UC-FIR-03: zaksięgowanie przelewu bankowego (przelew sparowany z zamówieniem
 * po tytule/ID — krok 2 scenariusza; AssignPaymentToOrder na kanwie kontekstu).
 *
 * Kontekst nie obsługuje płatności kartą/bramką ani gotówki (Assumptions na kanwie) —
 * jedyne źródło wpłat to przelewy z importowanego wyciągu bankowego.
 */
public record ProcessPaymentCommand(String orderId,
                                    String transactionId,
                                    BigDecimal amount,
                                    String currency) {

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
