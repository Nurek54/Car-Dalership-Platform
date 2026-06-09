package salon.sales.domain.model.offer;

import salon.sales.domain.model.customer.CustomerId;
import salon.shared.model.Money;
import salon.shared.model.SpecificationId;

/**
 * Fabryka agregatu Offer (węzeł "OfferFactory" w docs/Architecture/SalesArchitecture.md:
 * AppSvc --> OfferFactory -. creates .-> Offer).
 *
 * Odpowiada za poprawne utworzenie nowej oferty (nadanie tożsamości {@link OfferId}
 * i ewentualne ustawienie ceny bazowej), zdejmując ten obowiązek z warstwy aplikacji.
 */
public class OfferFactory {

    public Offer createOffer(CustomerId customerId, SpecificationId specificationId) {
        return new Offer(OfferId.generate(), customerId, specificationId);
    }

    public Offer createOffer(CustomerId customerId, SpecificationId specificationId, Money basePrice) {
        Offer offer = new Offer(OfferId.generate(), customerId, specificationId);
        if (basePrice != null) {
            offer.setBasePrice(basePrice);
        }
        return offer;
    }
}
