package salon.sales.infrastructure.out.persistence;

import salon.sales.application.domain.model.offer.Offer;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.port.out.OfferDatabaseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OUTBOUND ADAPTER (Figure 22: DBAdapter) — in-memory implementation of
 * {@link OfferDatabaseRepository}. Proforma offers keyed by the offer id.
 */
public class InMemoryOfferRepository implements OfferDatabaseRepository {

    private final Map<String, Offer> byId = new ConcurrentHashMap<>();

    @Override
    public void save(Offer offer) {
        this.byId.put(offer.getId().value(), offer);
    }

    @Override
    public Optional<Offer> findById(OfferId id) {
        return Optional.ofNullable(this.byId.get(id.value()));
    }

    @Override
    public List<Offer> findAll() {
        return new ArrayList<>(this.byId.values());
    }
}
