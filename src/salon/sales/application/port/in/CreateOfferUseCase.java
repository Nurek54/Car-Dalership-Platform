package salon.sales.application.port.in;

import salon.sales.domain.model.offer.OfferId;

// Port wejściowy dla UC-SPR-01.
public interface CreateOfferUseCase {
    OfferId createOffer(CreateOfferCommand command);
}
