package unit.sales_and_crm_context.aggregateTests;

import org.junit.jupiter.api.Test;
import salon.sales.application.domain.model.customer.CustomerId;
import salon.sales.application.domain.exception.InvalidOfferStateException;
import salon.sales.application.domain.exception.OfferImmutableException;
import salon.sales.application.domain.model.offer.Offer;
import salon.sales.application.domain.model.offer.OfferState;
import salon.sales.application.domain.model.offer.OfferId;
import salon.common.model.Money;
import salon.common.model.SpecificationId;

import static org.assertj.core.api.Assertions.*;

class OfferTest {

    @Test
    void shouldSuccessfullyPublishDraftOffer() {
        // A newly created offer always starts as DRAFT
        Offer offer = new Offer(
                new OfferId("O-200"),
                new CustomerId("C-001"),
                new SpecificationId("SPEC-1"),
                Money.of(120000, "PLN")
        );

        // The Salesperson finishes filling in the offer and publishes it
        offer.publishOffer();

        // The state changes to PUBLISHED
        assertThat(offer.state()).isEqualTo(OfferState.PUBLISHED);
    }

    @Test
    void shouldFailToAcceptOfferThatWasNeverPublished() {
        Offer offer = new Offer(
                new OfferId("O-201"),
                new CustomerId("C-001"),
                new SpecificationId("SPEC-1"),
                Money.of(120000, "PLN")
        );

        // An acceptance attempt by the customer must be blocked
        // This guards against a situation where the customer accepts terms that are "being edited"
        assertThatThrownBy(() -> offer.accept())
                .isInstanceOf(InvalidOfferStateException.class)
                .hasMessageContaining("Only PUBLISHED offers can be accepted");

        // The state remains unchanged
        assertThat(offer.state()).isEqualTo(OfferState.DRAFT);
    }

    @Test
    void shouldSuccessfullyRejectPublishedOffer() {
        Offer offer = new Offer(
                new OfferId("O-202"),
                new CustomerId("C-001"),
                new SpecificationId("SPEC-1"),
                Money.of(120000, "PLN")
        );
        offer.publishOffer(); // State: PUBLISHED

        // The customer rejects the offer
        offer.reject();

        // The state changes to REJECTED
        assertThat(offer.state()).isEqualTo(OfferState.REJECTED);
    }

    @Test
    void shouldPreventStateMutationAfterRejection() {
        // A rejected offer
        Offer offer = new Offer(
                new OfferId("O-203"),
                new CustomerId("C-001"),
                new SpecificationId("SPEC-1"),
                Money.of(120000, "PLN")
        );
        offer.publishOffer();
        offer.reject();

        // The customer calls to say they changed their mind after all.
        // The aggregate must block this - a rejected offer is a closed chapter, a new one must be made
        assertThatThrownBy(() -> offer.accept())
                .isInstanceOf(OfferImmutableException.class)
                .hasMessageContaining("Cannot change state of a REJECTED offer");
    }
}
