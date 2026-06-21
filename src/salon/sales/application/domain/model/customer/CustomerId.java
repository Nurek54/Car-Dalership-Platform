package salon.sales.application.domain.model.customer;

/**
 * Value Object: the Customer identifier in the Sales and CRM context.
 *
 * Shared by the {@code Customer} and {@code Offer} aggregates (see
 * docs/Architecture/SalesArchitecture.md and docs/Agregate/Sales/customer-offer-order.md),
 * which is why it lives in the {@code model.customer} package, not inside the offer package.
 *
 * It is a distinct type from the customer identifier in the Financing context
 * (salon.financing.domain.model.financing.CustomerId) — each Bounded Context
 * has its own model (a disjoint model).
 */
public record CustomerId(String value) {

    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CustomerId must not be blank.");
        }
    }
}
