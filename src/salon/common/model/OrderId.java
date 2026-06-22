package salon.common.model;

import java.util.UUID;

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
