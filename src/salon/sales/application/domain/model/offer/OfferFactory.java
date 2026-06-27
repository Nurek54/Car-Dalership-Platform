package salon.sales.application.domain.model.offer;

import salon.common.model.Money;
import salon.common.model.SpecificationId;
import salon.sales.application.domain.model.customer.CustomerId;

public class OfferFactory {

    
    public Offer createOffer(CustomerId customerId, SpecificationId specificationId, Money basePrice) {
        if (basePrice == null || basePrice.amount().signum() <= 0) {
            throw new InvalidOfferDataException("Cannot create offer with zero or negative base price");
        }
        return new Offer(OfferId.generate(), customerId, specificationId, basePrice);
    }

    
    public Offer createProforma(CustomerId customerId, SpecificationId specificationId, Money basePrice) {
        return createOffer(customerId, specificationId, basePrice);
    }
}
