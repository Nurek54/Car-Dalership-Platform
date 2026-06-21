package salon.common.model;

/**
 * Customer identity in the Shared Kernel — the counterpart of
 * the context-specific salon.sales.domain.model.customer.CustomerId, used where
 * the customer identifier crosses context boundaries.
 */
public record CustomerId(String value) {

    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CustomerId must not be blank.");
        }
    }
}
