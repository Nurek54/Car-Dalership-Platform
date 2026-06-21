package unit.sales_and_crm_context.factories;

import org.junit.jupiter.api.Test;
import salon.sales.application.domain.model.customer.CustomerId;
import salon.sales.application.domain.model.offer.InvalidOfferDataException;
import salon.sales.application.domain.model.offer.Offer;
import salon.sales.application.domain.model.offer.OfferFactory;
import salon.sales.application.domain.model.offer.OfferState;
import salon.common.model.*;

import static org.assertj.core.api.Assertions.*;

class OfferFactoryTest {

    private final OfferFactory offerFactory = new OfferFactory();

    @Test
    void shouldCreateValidDraftOffer() {
        // We have valid input data for the new offer
        CustomerId customerId = new CustomerId("CUST-1");
        SpecificationId specId = new SpecificationId("SPEC-1");
        Money basePrice = Money.of(150000, "PLN");

        Offer offer = offerFactory.createOffer(customerId, specId, basePrice);

        assertThat(offer).isNotNull();
        assertThat(offer.id()).isNotNull(); // The factory generates the ID itself
        assertThat(offer.state()).isEqualTo(OfferState.DRAFT);
        assertThat(offer.finalPrice()).isEqualTo(basePrice);
    }

    @Test
    void shouldRejectCreatingOfferForNegativePrice() {
        // An invalid, negative amount from the price list
        CustomerId customerId = new CustomerId("CUST-1");
        SpecificationId specId = new SpecificationId("SPEC-1");
        Money invalidPrice = Money.of(-100, "PLN");

        // The factory rejects the attempt to create the offer
        assertThatThrownBy(() -> offerFactory.createOffer(customerId, specId, invalidPrice))
                .isInstanceOf(InvalidOfferDataException.class)
                .hasMessageContaining("Cannot create offer with zero or negative base price");
    }
}