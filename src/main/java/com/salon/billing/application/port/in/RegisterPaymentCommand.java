package main.java.com.salon.billing.application.port.in;

import java.math.BigDecimal;

/**
 * Komenda (DTO wejściowe) dla UC-ROZ-01.
 * Przenosi surowe dane z zewnątrz; serwis aplikacyjny zamieni je na obiekty domenowe.
 * gatewayTransactionId jest nullem dla wpłaty rejestrowanej ręcznie przez Księgowego.
 */
public record RegisterPaymentCommand(String orderId,
                                     BigDecimal amount,
                                     String currency,
                                     BigDecimal orderValue,
                                     String gatewayTransactionId) {

    // Walidacja danych wejściowych — robimy ją "na wejściu" do warstwy aplikacji.
    public RegisterPaymentCommand {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null.");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency is required.");
        }
        if (orderValue == null) {
            throw new IllegalArgumentException("orderValue must not be null.");
        }
        // gatewayTransactionId może być nullem (wpłata ręczna) — dlatego go nie walidujemy.
    }
}
