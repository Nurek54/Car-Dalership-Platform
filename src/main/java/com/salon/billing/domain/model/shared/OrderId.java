package main.java.com.salon.billing.domain.model.shared;
/**
 * Value Object: identyfikator zamówienia z innego kontekstu (Sprzedaż).
 * W tym module odwołujemy się do zamówienia TYLKO przez to ID (zasada DDD).
 */
public record OrderId(String value) {

    public OrderId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OrderId nie może być pusty.");
        }
    }
}