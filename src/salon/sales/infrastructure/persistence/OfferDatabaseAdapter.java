package salon.sales.infrastructure.persistence;

import org.springframework.stereotype.Component;
import salon.sales.application.port.out.OfferRepository;
import salon.sales.domain.model.customer.CustomerId;
import salon.sales.domain.model.offer.Discount;
import salon.sales.domain.model.offer.Offer;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.model.offer.OfferState;
import salon.shared.infrastructure.persistence.DomainReflection;
import salon.shared.model.Money;
import salon.shared.model.SpecificationId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adapter wyjściowy (DatabaseAdapter) portu OfferRepository — mapowanie agregatu Offer
 * na model JPA. Stan i wycena odtwarzane przez DomainReflection (omijamy reguły maszyny
 * stanów przy rehydratacji); kwoty round-tripowane tekstowo (bez zmiany skali BigDecimal).
 * Wersja rekordu (@Version) wędruje z agregatem — zapis nieaktualnej kopii kończy się
 * ObjectOptimisticLockingFailureException (saveAndFlush wymusza weryfikację od razu).
 */
@Component
public class OfferDatabaseAdapter implements OfferRepository {

    private final OfferJpaRepository repository;

    public OfferDatabaseAdapter(OfferJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Offer offer) {
        repository.saveAndFlush(toEntity(offer));
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
            entity.basePriceAmount = offer.getBasePrice().amount().toPlainString();
            entity.basePriceCurrency = offer.getBasePrice().currency();
        }
        if (offer.getAppliedDiscount() != null) {
            entity.discountPercentage = offer.getAppliedDiscount().percentage();
        }
        if (offer.getFinalPrice() != null) {
            entity.finalPriceAmount = offer.getFinalPrice().amount().toPlainString();
            entity.finalPriceCurrency = offer.getFinalPrice().currency();
        }
        entity.validityDate = offer.getValidityDate();
        entity.state = offer.getState().name();
        entity.version = (Long) DomainReflection.get(offer, "version");
        return entity;
    }

    private Offer toDomain(OfferJpaEntity entity) {
        Offer offer = new Offer(
                new OfferId(entity.id),
                new CustomerId(entity.customerId),
                new SpecificationId(entity.specificationId));
        if (entity.basePriceAmount != null) {
            DomainReflection.set(offer, "basePrice",
                    new Money(new BigDecimal(entity.basePriceAmount), entity.basePriceCurrency));
        }
        if (entity.discountPercentage != null) {
            DomainReflection.set(offer, "appliedDiscount", new Discount(entity.discountPercentage));
        }
        if (entity.finalPriceAmount != null) {
            DomainReflection.set(offer, "finalPrice",
                    new Money(new BigDecimal(entity.finalPriceAmount), entity.finalPriceCurrency));
        }
        if (entity.validityDate != null) {
            DomainReflection.set(offer, "validityDate", entity.validityDate);
        }
        DomainReflection.set(offer, "state", OfferState.valueOf(entity.state));
        DomainReflection.set(offer, "version", entity.version);
        return offer;
    }
}
