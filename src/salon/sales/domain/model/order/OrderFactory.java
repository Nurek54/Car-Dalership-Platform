package salon.sales.domain.model.order;

import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.model.offer.OfferSnapshot;
import salon.shared.model.OrderId;

/**
 * Fabryka agregatu Order (węzeł "OrderFactory" w docs/Architecture/SalesArchitecture.md,
 * opisana w docs/Agregate/Sales/order.md).
 *
 * Buduje poprawne Zamówienie z migawki oferty ({@link OfferSnapshot}). Zgodnie z
 * docs/Agregate/Guidelines/value-object-audit.md fabryka wyciąga z oferty wyłącznie
 * niemutowalne obiekty wartości (OfferId, finalPrice) — NIE przyjmuje referencji do
 * agregatu Offer, dzięki czemu nie przenosimy referencji między korzeniami agregatów.
 *
 * Walidację stanu oferty (musi być PUBLISHED) wykonuje warstwa aplikacji PRZED
 * zbudowaniem migawki (patrz OrderAppService#createOrderFromOffer).
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
                    "Snapshot offerId does not match the provided offerId.");
        }
        // requiredDeposit = finalPrice z oferty (może być null, jeśli nie wyceniono).
        return new Order(OrderId.generate(), offerId, snapshot.finalPrice());
    }
}
