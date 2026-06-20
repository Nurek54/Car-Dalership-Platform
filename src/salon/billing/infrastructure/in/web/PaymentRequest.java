package salon.billing.infrastructure.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** DTO wejściowe zaksięgowania wpłaty (UC-FIR-03). Walidacja: kwota dodatnia. */
public record PaymentRequest(
        @NotBlank(message = "Identyfikator zamówienia jest wymagany") String orderId,
        @NotBlank(message = "Identyfikator transakcji jest wymagany") String transactionId,
        @NotNull(message = "Kwota jest wymagana")
        @Positive(message = "Kwota musi być większa od zera") BigDecimal amount,
        @NotBlank(message = "Waluta jest wymagana") String currency) {
}
