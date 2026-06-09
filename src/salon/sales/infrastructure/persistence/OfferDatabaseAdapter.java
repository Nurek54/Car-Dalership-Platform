package salon.sales.infrastructure.persistence;

import salon.sales.infrastructure.persistence.OfferJpaEntity;
import salon.sales.infrastructure.persistence.OfferJpaRepository;

import salon.shared.infrastructure.persistence.DomainReflection;
import org.springframework.stereotype.Component;
import salon.sales.application.port.out.OfferRepository;
import salon.sales.domain.model.customer.CustomerId;
import salon.sales.domain.model.offer.Discount;
import salon.sales.domain.model.offer.Offer;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.model.offer.OfferState;
import salon.shared.model.Money;
import salon.shared.model.SpecificationId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adapter sterowany (driven) — persystencja oferty (port {@link OfferRepository}).
 *
 * Cena końcowa, rabat i stan (np. PENDING_DIRECTOR_APPROVAL) zależą od reguł domeny i wejściowego
 * limitu rabatu, którego nie przechowujemy; dlatego zmapowany stan odtwarzamy refleksją
 * w infrastrukturze, nie dotykając kodu kontekstu Sprzedaży.
 */
@Component
public class OfferDatabaseAdapter implements OfferRepository {

    private final OfferJpaRepository repository;

    public OfferDatabaseAdapter(OfferJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Offer offer) {
        repository.save(toEntity(offer));
    }

    @Override
    public Optional<Offer> findById(OfferId id) {
        return repository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<Offer> findAll() {
        List<OfferJpaEntity> entities = repository.findAll();
        List<Offer> result = new ArrayList<>();
        for (int i = 0; i < entities.size(); i++) {
            result.add(toDomain(entities.get(i)));
        }
        return result;
    }

    private OfferJpaEntity toEntity(Offer offer) {
        OfferJpaEntity entity = new OfferJpaEntity();
        entity.id = offer.getId().value();
        entity.customerId = offer.getCustomerId().value();
        entity.specificationId = offer.getSpecificationId().value();
        if (offer.getBasePrice() != null) {
            entity.basePriceAmount = offer.getBasePrice().amount();
            entity.basePriceCurrency = offer.getBasePrice().currency();
        }
        if (offer.getAppliedDiscount() != null) {
            entity.discountPercentage = offer.getAppliedDiscount().percentage();
        }
        if (offer.getFinalPrice() != null) {
            entity.finalPriceAmount = offer.getFinalPrice().amount();
            entity.finalPriceCurrency = offer.getFinalPrice().currency();
        }
        entity.validityDate = offer.getValidityDate();
        entity.state = offer.getState().name();
        return entity;
    }

    private Offer toDomain(OfferJpaEntity entity) {
        Offer offer = new Offer(
                new OfferId(entity.id),
                new CustomerId(entity.customerId),
                new SpecificationId(entity.specificationId));
        if (entity.basePriceAmount != null) {
            DomainReflection.set(offer, "basePrice", Money.of(entity.basePriceAmount, entity.basePriceCurrency));
        }
        if (entity.discountPercentage != null) {
            DomainReflection.set(offer, "appliedDiscount", new Discount(entity.discountPercentage));
        }
        if (entity.finalPriceAmount != null) {
            DomainReflection.set(offer, "finalPrice", Money.of(entity.finalPriceAmount, entity.finalPriceCurrency));
        }
        if (entity.validityDate != null) {
            DomainReflection.set(offer, "validityDate", entity.validityDate);
        }
        DomainReflection.set(offer, "state", OfferState.valueOf(entity.state));
        return offer;
    }
}
