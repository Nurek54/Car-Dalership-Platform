package salon.sales.application.domain.model.offer;

import salon.sales.application.domain.model.customer.CustomerId;
import salon.common.model.Money;
import salon.common.model.SpecificationId;

/**
 * Value Object: an immutable snapshot of the offer passed to {@code OrderFactory}
 * when creating the Order (UC-SPR-02).
 *
 * Per docs/Agregate/Sales/order.md and docs/Agregate/Guidelines/value-object-audit.md
 * the Order factory uses ONLY the immutable value objects of the offer —
 * NOT a reference to the Offer aggregate — so as not to carry references between aggregate roots.
 *
 * {@code finalPrice} may be null if the offer was not priced.
 */
public record OfferSnapshot(OfferId offerId,
                            CustomerId customerId,
                            SpecificationId specificationId,
                            Money finalPrice) {

    public OfferSnapshot {
        if (offerId == null) {
            throw new IllegalArgumentException("offerId must not be null.");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null.");
        }
        if (specificationId == null) {
            throw new IllegalArgumentException("specificationId must not be null.");
        }
    }
}
