package salon.sales.infrastructure.web;

import jakarta.validation.constraints.NotBlank;

/** DTO wejściowe konwersji oferty w zamówienie (UC-SPR-02). Podpis klienta jest wymagany. */
public record CreateOrderRequest(
        @NotBlank(message = "Identyfikator oferty jest wymagany") String offerId,
        @NotBlank(message = "Podpis klienta jest wymagany") String customerSignature) {
}
