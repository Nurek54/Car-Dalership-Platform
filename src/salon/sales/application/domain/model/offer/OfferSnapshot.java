package salon.sales.application.domain.model.offer;

import salon.common.model.Money;
import salon.common.model.SpecificationId;
import salon.sales.application.domain.model.customer.CustomerId;

public record OfferSnapshot(OfferId offerId, CustomerId customerId,
                            SpecificationId specificationId, Money finalPrice) {

    public OfferSnapshot {
        if (offerId == null) {
            throw new IllegalArgumentException("offerId must not be null.");
        }
        if (finalPrice == null) {
            throw new IllegalArgumentException("finalPrice must not be null.");
        }
    }
}
