package salon.sales.infrastructure.out.persistence;

import org.springframework.stereotype.Repository;
import salon.common.model.Money;
import salon.common.model.SpecificationId;
import salon.sales.application.domain.model.customer.CustomerId;
import salon.sales.application.domain.model.offer.Offer;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.port.out.OfferDatabaseRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * OUTBOUND ADAPTER (Figure 22: DBAdapter) — JPA implementation of {@link OfferDatabaseRepository}.
 * Maps the rich {@link Offer} aggregate to/from {@link OfferEntity} and carries the optimistic-lock
 * version across load -> modify -> save.
 */
@Repository
public class OfferDatabaseAdapter implements OfferDatabaseRepository {

    private final OfferJpaRepository jpa;

    public OfferDatabaseAdapter(OfferJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Offer offer) {
        this.jpa.saveAndFlush(toEntity(offer));
    }

    @Override
    public Optional<Offer> findById(OfferId id) {
        return this.jpa.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<Offer> findAll() {
        return this.jpa.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    private OfferEntity toEntity(Offer offer) {
        return new OfferEntity(
                offer.getId().value(),
                offer.getCustomerId().value(),
                offer.getSpecificationId().value(),
                offer.getBasePrice().amount(), offer.getBasePrice().currency(),
                offer.getFinalPrice().amount(), offer.getFinalPrice().currency(),
                offer.getState(), offer.getValidityDate(), offer.getVersion());
    }

    private Offer toDomain(OfferEntity e) {
        return Offer.reconstitute(
                new OfferId(e.getId()),
                new CustomerId(e.getCustomerId()),
                new SpecificationId(e.getSpecificationId()),
                Money.of(e.getBaseAmount(), e.getBaseCurrency()),
                Money.of(e.getFinalAmount(), e.getFinalCurrency()),
                e.getState(), e.getValidityDate(), e.getVersion());
    }
}
