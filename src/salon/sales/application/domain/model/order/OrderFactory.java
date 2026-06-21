package salon.sales.application.domain.model.order;

import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.domain.model.offer.OfferSnapshot;
import salon.common.model.OrderId;

/**
 * Factory of the Order aggregate (the "OrderFactory" node in docs/Architecture/SalesArchitecture.md,
 * PDF chapter 3.3.3 "Aggregate Transformation").
 *
 * Builds a valid Order from the snapshot of an accepted offer ({@link OfferSnapshot}).
 * Per docs/Agregate/Guidelines/value-object-audit.md the factory extracts from the offer
 * only the immutable value objects (OfferId, finalPrice) — it does NOT accept
 * a reference to the Offer aggregate. The "only from an ACCEPTED offer" rule is enforced by
 * the Offer aggregate in the toSnapshot() method.
 */
public class OrderFactory {

    public Order createFromOffer(OfferId offerId, OfferSnapshot snapshot) {
        if (offerId == null) {
            throw new IllegalArgumentException("offerId must not be null.");
        }
        if (snapshot == null) {
            throw new IllegalArgumentException("snapshot must not be null.");
        }
        if (!offerId.equals(snapshot.offerId())) {
            throw new IllegalArgumentException(
                    "Snapshot offerId does not match the provided offerId");
        }
        // requiredDeposit = finalPrice from the offer (may be null if not priced).
        // specificationId from the snapshot — it will flow in OrderPlacedEvent to Inventory (UC-INW-01/02).
        Order order = new Order(OrderId.generate(), offerId,
                snapshot.specificationId(), snapshot.finalPrice());
        // The formal placement of the order is announced by the aggregate (OrderPlacedEvent — among others for Billing).
        order.markPlaced();
        return order;
    }
}
