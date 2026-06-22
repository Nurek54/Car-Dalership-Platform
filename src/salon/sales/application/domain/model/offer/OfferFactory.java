package salon.sales.application.domain.model.offer;

import salon.common.model.Money;
import salon.common.model.SpecificationId;
import salon.sales.application.domain.model.customer.CustomerId;

public class OfferFactory {

    public Offer createProforma(CustomerId customerId, SpecificationId specificationId, Money basePrice) {
        return new Offer(OfferId.generate(), customerId, specificationId, basePrice);
    }
}
