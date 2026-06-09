package salon.sales.application.service;

import salon.sales.application.port.in.CreateOfferCommand;
import salon.sales.application.port.in.CreateOfferUseCase;
import salon.sales.application.port.out.OfferRepository;
import salon.sales.domain.model.customer.CustomerId;
import salon.sales.domain.model.offer.Discount;
import salon.sales.domain.model.offer.DiscountLimit;
import salon.sales.domain.model.offer.Offer;
import salon.sales.domain.model.offer.OfferFactory;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.model.offer.OfferState;
import salon.shared.model.Money;
import salon.shared.model.SpecificationId;

import java.time.LocalDate;
import java.util.List;

/**
 * Realizuje UC-SPR-01 (orkiestracja). Tworzy ofertę (przez {@link OfferFactory}),
 * opcjonalnie wycenia i przyznaje rabat, a regułę limitu rabatu zostawia
 * agregatowi (Offer.applyDiscount).
 */
public class OfferAppService implements CreateOfferUseCase {

    private final OfferRepository offerRepository;
    private final OfferFactory offerFactory;

    public OfferAppService(OfferRepository offerRepository) {
        this(offerRepository, new OfferFactory());
    }

    public OfferAppService(OfferRepository offerRepository, OfferFactory offerFactory) {
        if (offerRepository == null) {
            throw new IllegalArgumentException("offerRepository must not be null.");
        }
        if (offerFactory == null) {
            throw new IllegalArgumentException("offerFactory must not be null.");
        }
        this.offerRepository = offerRepository;
        this.offerFactory = offerFactory;
    }

    @Override
    public OfferId createOffer(CreateOfferCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null.");
        }

        Money basePrice = null;
        if (command.basePrice() != null && command.currency() != null) {
            basePrice = new Money(command.basePrice(), command.currency());
        }

        Offer offer = offerFactory.createOffer(
                new CustomerId(command.customerId()),
                new SpecificationId(command.specificationId()),
                basePrice);

        if (command.discountPercentage() != null && command.salespersonDiscountLimit() != null) {
            offer.applyDiscount(
                    new Discount(command.discountPercentage()),
                    new DiscountLimit(command.salespersonDiscountLimit()));
        }

        offerRepository.save(offer);
        return offer.getId();
    }

    /**
     * Cron (ExpiredOffersCronJobAdapter, UC-SPR-01 A2): oferty po terminie ważności -> EXPIRED.
     */
    public void processExpiredOffers() {
        LocalDate today = LocalDate.now();
        List<Offer> all = offerRepository.findAll();
        for (int i = 0; i < all.size(); i++) {
            Offer offer = all.get(i);
            if (isOpen(offer) && offer.getValidityDate().isBefore(today)) {
                offer.expire();
                offerRepository.save(offer);
            }
        }
    }

    /**
     * Reakcja na CatalogUpdated (CatalogUpdatedEvent): nowa wersja cennika unieważnia otwarte oferty
     * zbudowane na poprzednich cenach.
     *
     * UWAGA: agregat Offer nie przechowuje dziś CatalogId, więc konserwatywnie unieważniamy
     * WSZYSTKIE otwarte oferty. Docelowo: dodać snapshot CatalogId do Offer i filtrować po nim.
     */
    public void invalidateOffersForOlderCatalogs(String catalogId) {
        if (catalogId == null || catalogId.isBlank()) {
            throw new IllegalArgumentException("catalogId must not be blank.");
        }
        List<Offer> all = offerRepository.findAll();
        for (int i = 0; i < all.size(); i++) {
            Offer offer = all.get(i);
            if (isOpen(offer)) {
                offer.expire();
                offerRepository.save(offer);
            }
        }
    }

    // "Otwarta" oferta to taka, którą można jeszcze unieważnić (nie CONVERTED i nie EXPIRED).
    private boolean isOpen(Offer offer) {
        OfferState state = offer.getState();
        return state == OfferState.DRAFT
                || state == OfferState.PUBLISHED
                || state == OfferState.PENDING_DIRECTOR_APPROVAL;
    }
}
