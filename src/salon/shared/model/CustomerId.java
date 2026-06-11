package salon.shared.model;

/**
 * Tożsamość klienta we Wspólnym Rdzeniu (Shared Kernel) — odpowiednik
 * kontekstowego salon.sales.domain.model.customer.CustomerId, używany tam,
 * gdzie identyfikator klienta przekracza granice kontekstów.
 */
public record CustomerId(String value) {

    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CustomerId must not be blank.");
        }
    }
}
