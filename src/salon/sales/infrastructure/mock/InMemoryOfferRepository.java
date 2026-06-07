package salon.sales.infrastructure.mock;

import salon.sales.application.port.out.OfferRepository;
import salon.sales.domain.model.offer.Offer;
import salon.sales.domain.model.offer.OfferId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryOfferRepository implements OfferRepository {

    private final Map<OfferId, Offer> store = new HashMap<>();

    @Override
    public void save(Offer offer) {
        this.store.put(offer.getId(), offer);
    }

    @Override
    public Optional<Offer> findById(OfferId id) {
        return Optional.ofNullable(this.store.get(id));
    }

    @Override
    public List<Offer> findAll() {
        return new ArrayList<>(this.store.values());
    }
}
