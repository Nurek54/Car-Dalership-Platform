package salon.financing.application.domain.model.financing;

/**
 * Value object: a disjoint reference to a customer from the Sales and CRM Context (Figure 43).
 * Financing has its own customer-identifier model (each Bounded Context defines its own).
 */
public record CustomerId(String value) {

    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CustomerId must not be blank.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
