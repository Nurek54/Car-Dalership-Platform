package salon.financing.application.domain.model.financing;

/**
 * Value object: a disjoint reference to an order from the Sales Context (Figure 43).
 * Each Bounded Context has its own identifier model — Financing references
 * the order only through this value.
 */
public record OrderId(String value) {

    public OrderId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OrderId must not be blank.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
