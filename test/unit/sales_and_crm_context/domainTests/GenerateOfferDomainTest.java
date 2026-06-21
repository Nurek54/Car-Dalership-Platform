package unit.sales_and_crm_context.domainTests;

import org.junit.jupiter.api.Test;
import salon.sales.application.domain.model.customer.CustomerId;
import salon.sales.application.domain.model.offer.InvalidOfferDataException;
import salon.sales.application.domain.model.offer.Offer;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.domain.model.offer.OfferState;
import salon.common.model.*;

import static org.assertj.core.api.Assertions.*;

/** UC-CRM-02: Generating the proforma offer */
class GenerateOfferDomainTest {

    @Test
    void shouldCreateAndPublishOfferSuccessfully() {
        // We create a new offer for a specific customer and specification
        Offer offer = new Offer(new OfferId("O-1"), new CustomerId("C-1"), new SpecificationId("S-1"), Money.of(100000, "PLN"));

        // The Salesperson decides to publish the offer
        offer.publishOffer();

        // The state machine state correctly changes to PUBLISHED
        assertThat(offer.state()).isEqualTo(OfferState.PUBLISHED);
    }

    @Test
    void shouldRejectOfferCreationWithZeroOrNegativeValue() {
        // An attempt to create an offer with a negative or zero amount
        // is a violation of a basic business rule. The aggregate must block this in the constructor.
        assertThatThrownBy(() -> new Offer(new OfferId("O-2"), new CustomerId("C-1"), new SpecificationId("S-2"), Money.of(-500, "PLN")))
                .isInstanceOf(InvalidOfferDataException.class)
                .hasMessageContaining("Offer price must be strictly positive");
    }
}