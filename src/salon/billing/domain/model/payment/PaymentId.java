package salon.billing.domain.model.payment;

import java.util.UUID;

// Value Object: tożsamość agregatu Payment. Teraz oparty na String (test: new PaymentId("PAY-001")).
public record PaymentId(String value) {

    public PaymentId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PaymentId must not be blank.");
        }
    }

    public static PaymentId generate() {
        return new PaymentId("PAY-" + UUID.randomUUID());
    }
}
