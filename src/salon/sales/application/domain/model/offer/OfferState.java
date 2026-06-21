package salon.sales.application.domain.model.offer;

/**
 * The life cycle of the offer (proforma) — per the aggregate model
 * (docs/Agregate/Sales/customer-offer-order.md) and the PDF (chapter 3.3.4):
 * the offer ends its life in an unambiguous terminal state ACCEPTED or REJECTED,
 * after which it becomes an immutable, historical record of the negotiated terms.
 */
public enum OfferState {
    DRAFT,      // "In preparation" — a draft, can be priced and discounted
    PUBLISHED,  // "Created" — generated and presented to the customer
    ACCEPTED,   // "Accepted" — the customer accepted the terms (terminal state)
    REJECTED    // "Rejected" — the customer declined (terminal state)
}
