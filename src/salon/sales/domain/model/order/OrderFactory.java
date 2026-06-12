package salon.sales.domain.model.order;

import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.model.offer.OfferSnapshot;
import salon.shared.model.OrderId;

/**
 * Fabryka agregatu Order (węzeł "OrderFactory" w docs/Architecture/SalesArchitecture.md,
 * PDF rozdz. 3.3.3 "Transformacja Agregatów").
 *
 * Buduje poprawne Zamówienie z migawki zaakceptowanej oferty ({@link OfferSnapshot}).
 * Zgodnie z docs/Agregate/Guidelines/value-object-audit.md fabryka wyciąga z oferty
 * wyłącznie niemutowalne obiekty wartości (OfferId, finalPrice) — NIE przyjmuje
 * referencji do agregatu Offer. Regułę "tylko z oferty ACCEPTED" egzekwuje sam
 * agregat Offer w metodzie toSnapshot().
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
        // requiredDeposit = finalPrice z oferty (może być null, jeśli nie wyceniono).
        // specificationId z migawki — popłynie w OrderPlacedEvent do Inwentarza (UC-INW-01/02).
        Order order = new Order(OrderId.generate(), offerId,
                snapshot.specificationId(), snapshot.finalPrice());
        // Formalne złożenie zamówienia ogłasza agregat (OrderPlacedEvent — m.in. dla Rozliczeń).
        order.markPlaced();
        return order;
    }
}
