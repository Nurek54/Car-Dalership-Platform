package salon.sales.application.domain.model.order;

/**
 * Enumeration (Figure 23) — settlement status of an Order, kept in sync with the Billing Context.
 *
 * PENDING – awaiting the deposit; PAID – deposit/contract value settled; REFUNDED – money returned
 * after a cancellation.
 */
public enum PaymentStatus {
    PENDING,
    PAID,
    REFUNDED
}
