package salon.sales.application.domain.model.customer;

import java.util.UUID;

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
