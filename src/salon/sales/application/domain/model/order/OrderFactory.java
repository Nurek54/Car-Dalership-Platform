package salon.sales.application.domain.model.order;

import salon.common.model.OrderId;
import salon.sales.application.domain.exception.InvalidOfferStateException;
import salon.sales.application.domain.model.offer.Offer;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.domain.model.offer.OfferSnapshot;
import salon.sales.application.domain.model.offer.OfferState;

/**
 * Factory for the {@link Order} aggregate — the single creation path (UC-CRM-03).
 * Enforces the invariant that an order may be created only from an ACCEPTED offer and copies the
 * required deposit (final price) from the offer snapshot.
 */
public class OrderFactory {

    /** Creates an order from an in-memory accepted {@link Offer} aggregate. */
    public Order createFromOffer(Offer offer) {
        if (offer == null) {
            throw new IllegalArgumentException("offer must not be null.");
        }
        if (offer.getState() != OfferState.ACCEPTED) {
            throw new InvalidOfferStateException(
                    "An order can be created only from an ACCEPTED offer (current: " + offer.getState() + ").");
        }
        return new Order(OrderId.generate(), offer.getId(), offer.getFinalPrice());
    }

    /** Creates an order from an offer id + its read-only snapshot (cross-aggregate creation). */
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
