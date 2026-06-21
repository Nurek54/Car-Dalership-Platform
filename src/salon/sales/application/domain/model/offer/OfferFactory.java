package salon.sales.application.domain.model.offer;

import salon.sales.application.domain.model.customer.CustomerId;
import salon.common.model.Money;
import salon.common.model.SpecificationId;

/**
 * Factory of the Offer aggregate (the "OfferFactory" node in docs/Architecture/SalesArchitecture.md:
 * AppSvc --> OfferFactory -. creates .-> Offer).
 *
 * Responsible for the correct creation of a new offer: it assigns the identity {@link OfferId}
 * and validates the input (the base price from the price list must be strictly positive),
 * taking this duty off the application layer.
 */
public class OfferFactory {

    public Offer createOffer(CustomerId customerId, SpecificationId specificationId) {
        return new Offer(OfferId.generate(), customerId, specificationId);
    }

    public Offer createOffer(CustomerId customerId, SpecificationId specificationId, Money basePrice) {
        if (basePrice != null && basePrice.amount().signum() <= 0) {
            throw new InvalidOfferDataException(
                    "Cannot create offer with zero or negative base price");
        }
        if (basePrice == null) {
            return new Offer(OfferId.generate(), customerId, specificationId);
        }
        return new Offer(OfferId.generate(), customerId, specificationId, basePrice);
    }
}
