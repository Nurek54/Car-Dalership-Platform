package salon.sales.application.domain.model.offer;

import salon.sales.application.domain.model.customer.CustomerId;
import salon.common.model.Money;
import salon.common.model.SpecificationId;

/**
 * Fabryka agregatu Offer (węzeł "OfferFactory" w docs/Architecture/SalesArchitecture.md:
 * AppSvc --> OfferFactory -. creates .-> Offer).
 *
 * Odpowiada za poprawne utworzenie nowej oferty: nadaje tożsamość {@link OfferId}
 * i weryfikuje dane wejściowe (cena bazowa z cennika musi być ściśle dodatnia),
 * zdejmując ten obowiązek z warstwy aplikacji.
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
