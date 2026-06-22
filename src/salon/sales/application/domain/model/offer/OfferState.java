package salon.sales.application.domain.model.offer;

/**
 * Enumeration (Figure 23) — lifecycle state of an Offer.
 *
 * DRAFT – "in preparation"; PUBLISHED – "created" and presented to the customer (UC-CRM-02);
 * ACCEPTED – customer accepted (UC-CRM-03); REJECTED – customer declined (UC-CRM-03 / A1).
 */
public enum OfferState {
    DRAFT,
    PUBLISHED,
    ACCEPTED,
    REJECTED
}
