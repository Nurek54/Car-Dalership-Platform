package main.java.com.salon.billing.domain.model.payment;

import java.util.UUID;

// Value Object: tożsamość agregatu Payment.
public record PaymentId(UUID value) {

    public PaymentId {
        if (value == null) {
            throw new IllegalArgumentException("PaymentId must not be null.");
        }
    }

    // Fabryka: nowy losowy identyfikator dla nowej wpłaty.
    public static PaymentId generate() {
        return new PaymentId(UUID.randomUUID());
    }
}
