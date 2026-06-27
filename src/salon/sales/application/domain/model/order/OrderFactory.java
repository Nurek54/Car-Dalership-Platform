package salon.sales.application.domain.model.order;

import salon.common.model.OrderId;
import salon.sales.application.domain.exception.InvalidOfferStateException;
import salon.sales.application.domain.model.offer.Offer;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.domain.model.offer.OfferSnapshot;
import salon.sales.application.domain.model.offer.OfferState;

public class OrderFactory {

    
    public Order createFromOffer(Offer offer) {
        if (offer == null) {
            throw new IllegalArgumentException("offer must not be null.");
        }
        if (offer.state() != OfferState.ACCEPTED) {
            throw new InvalidOfferStateException(
                    "An order can be created only from an ACCEPTED offer (current: " + offer.state() + ").");
        }
        return new Order(OrderId.generate(), offer.id(), offer.finalPrice());
    }

    
    public Order createFromOffer(OfferId offerId, OfferSnapshot snapshot) {
        if (offerId == null) {
            throw new IllegalArgumentException("offerId must not be null.");
        }
        if (snapshot == null) {
            throw new IllegalArgumentException("snapshot must not be null.");
        }
        if (!offerId.equals(snapshot.offerId())) {
            throw new IllegalArgumentException("Snapshot offerId does not match the provided offerId");
        }
        return new Order(OrderId.generate(), offerId, snapshot.finalPrice());
    }
}
