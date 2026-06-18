package salon.sales.application.domain.model.offer;

import salon.sales.application.domain.model.customer.CustomerId;
import salon.common.model.Money;
import salon.common.model.SpecificationId;

/**
 * Value Object: niemutowalna migawka oferty przekazywana do {@code OrderFactory}
 * przy tworzeniu Zamówienia (UC-SPR-02).
 *
 * Zgodnie z docs/Agregate/Sales/order.md oraz docs/Agregate/Guidelines/value-object-audit.md
 * fabryka Zamówienia korzysta WYŁĄCZNIE z niemutowalnych obiektów wartości oferty —
 * NIE z referencji do agregatu Offer — aby nie przenosić referencji między korzeniami agregatów.
 *
 * {@code finalPrice} może być null, jeśli oferty nie wyceniono.
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
