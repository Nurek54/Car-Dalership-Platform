package salon.common.model;

import java.util.UUID;

/**
 * Value Object: an order identifier. Shared kernel — used by both
 * Billing (a reference to an order from another context) and Sales (its own Order aggregate).
 * Per DDD we reference the order ONLY through this ID (a disjoint model).
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
