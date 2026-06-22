package salon.sales.application.domain.model.order;

import salon.common.model.OrderId;
import salon.sales.application.domain.exception.InvalidOfferStateException;
import salon.sales.application.domain.model.offer.Offer;
import salon.sales.application.domain.model.offer.OfferState;

public class OrderFactory {

    public Order createFromOffer(Offer offer) {
        if (offer == null) {
            throw new IllegalArgumentException("offer must not be null.");
        }
        if (offer.getState() != OfferState.ACCEPTED) {
            throw new InvalidOfferStateException(
                    "An order can be created only from an ACCEPTED offer (current: " + offer.getState() + ").");
        }
        return new Order(OrderId.generate(), offer.getId());
    }
}
