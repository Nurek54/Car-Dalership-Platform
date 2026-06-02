package salon.sales.application.port.out;

import salon.sales.domain.model.offer.Offer;
import salon.sales.domain.model.offer.OfferId;

import java.util.Optional;

public interface OfferRepository {
    void save(Offer offer);
    Optional<Offer> findById(OfferId id);
}
