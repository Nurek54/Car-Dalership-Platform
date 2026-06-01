package salon.shared.model;

import java.util.UUID;

/**
 * Value Object: identyfikator zamówienia. Wspólny rdzeń — używają go zarówno
 * Rozliczenia (odwołanie do zamówienia z innego kontekstu) jak i Sprzedaż (własny agregat Order).
 * Zgodnie z DDD odwołujemy się do zamówienia TYLKO przez to ID (model rozłączny).
 */
public record OrderId(String value) {

    public OrderId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OrderId must not be blank.");
        }
    }

    public static OrderId generate() {
        return new OrderId("ORD-" + UUID.randomUUID());
    }
}
