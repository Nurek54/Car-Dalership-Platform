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
}