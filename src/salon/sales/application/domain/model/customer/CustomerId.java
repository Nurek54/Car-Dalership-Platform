package salon.sales.application.domain.model.customer;

import java.util.UUID;

/**
 * Value Object (Figure 23) — the Sales-local identity of a Customer.
 * Crosses to the shared kernel (salon.common.model.CustomerId) only at context boundaries.
 */
public record CustomerId(String value) {

    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CustomerId must not be blank.");
        }
    }

    public static CustomerId generate() {
        return new CustomerId("CUST-" + UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value;
    }
}
