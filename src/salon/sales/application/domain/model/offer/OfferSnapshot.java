package salon.sales.application.domain.model.offer;

import salon.common.model.Money;
import salon.common.model.SpecificationId;
import salon.sales.application.domain.model.customer.CustomerId;

/**
 * Immutable snapshot of an accepted {@link Offer}, passed to the {@link salon.sales.application.domain.model.order.OrderFactory}
 * so the Order can be created from the offer's data without holding a reference to the aggregate.
 */
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
