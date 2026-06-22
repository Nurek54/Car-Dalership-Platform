package salon.sales.application.port.out;

import salon.sales.application.domain.model.offer.Offer;
import salon.sales.application.domain.model.offer.OfferId;

import java.util.List;
import java.util.Optional;

/**
 * OUTBOUND PORT (Figure 22) — "OfferDatabaseRepository". Persistence of the Offer aggregate.
 */
public interface OfferDatabaseRepository {

    void save(Offer offer);

    Optional<Offer> findById(OfferId id);

    List<Offer> findAll();
}
