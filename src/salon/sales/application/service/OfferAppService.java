package salon.sales.application.service;

import salon.sales.application.port.in.CreateOfferCommand;
import salon.sales.application.port.in.CreateOfferUseCase;
import salon.sales.application.port.out.OfferRepository;
import salon.sales.domain.model.offer.CustomerId;
import salon.sales.domain.model.offer.Discount;
import salon.sales.domain.model.offer.DiscountLimit;
import salon.sales.domain.model.offer.Offer;
import salon.sales.domain.model.offer.OfferId;
import salon.shared.model.Money;
import salon.shared.model.SpecificationId;

/**
 * Realizuje UC-SPR-01 (orkiestracja). Tworzy ofertę, opcjonalnie wycenia i przyznaje rabat,
 * a regułę limitu rabatu zostawia agregatowi (Offer.applyDiscount).
 */
public class OfferAppService implements CreateOfferUseCase {

    private final OfferRepository offerRepository;

    public OfferAppService(OfferRepository offerRepository) {
        if (offerRepository == null) {
            throw new IllegalArgumentException("offerRepository must not be null.");
        }
        this.offerRepository = offerRepository;
    }

    @Override
    public OfferId createOffer(CreateOfferCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null.");
        }

        Offer offer = new Offer(
                OfferId.generate(),
                new CustomerId(command.customerId()),
                new SpecificationId(command.specificationId()));

        if (command.basePrice() != null && command.currency() != null) {
            offer.setBasePrice(new Money(command.basePrice(), command.currency()));
        }

        if (command.discountPercentage() != null && command.salespersonDiscountLimit() != null) {
            offer.applyDiscount(
                    new Discount(command.discountPercentage()),
                    new DiscountLimit(command.salespersonDiscountLimit()));
        }

        offerRepository.save(offer);
        return offer.getId();
    }
}
