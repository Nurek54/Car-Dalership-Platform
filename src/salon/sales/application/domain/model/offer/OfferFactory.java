package salon.sales.application.domain.model.offer;

import salon.common.model.Money;
import salon.common.model.SpecificationId;
import salon.sales.application.domain.model.customer.CustomerId;

/**
 * Factory for the {@link Offer} aggregate — the single creation path for a new proforma offer
 * (UC-CRM-02). A fresh offer starts in DRAFT with a newly generated identity.
 */
public class OfferFactory {

    /** Creates a fresh DRAFT proforma offer; rejects a zero/negative base price up front. */
    public Offer createOffer(CustomerId customerId, SpecificationId specificationId, Money basePrice) {
        if (basePrice == null || basePrice.amount().signum() <= 0) {
            throw new InvalidOfferDataException("Cannot create offer with zero or negative base price");
        }
        return new Offer(OfferId.generate(), customerId, specificationId, basePrice);
    }

    /** Backward-compatible alias used by the application service. */
    public Offer createProforma(CustomerId customerId, SpecificationId specificationId, Money basePrice) {
        return createOffer(customerId, specificationId, basePrice);
    }
}
