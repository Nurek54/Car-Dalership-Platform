package salon.sales.application.domain.model.offer;

import salon.common.model.Money;
import salon.common.model.SpecificationId;
import salon.sales.application.domain.model.customer.CustomerId;

/**
 * Factory for the {@link Offer} aggregate — the single creation path for a new proforma offer
 * (UC-CRM-02). A fresh offer starts in DRAFT with a newly generated identity.
 */
public class OfferFactory {

    public Offer createProforma(CustomerId customerId, SpecificationId specificationId, Money basePrice) {
        return new Offer(OfferId.generate(), customerId, specificationId, basePrice);
    }
}
