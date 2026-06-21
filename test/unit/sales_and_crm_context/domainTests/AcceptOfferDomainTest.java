package unit.sales_and_crm_context.domainTests;

import org.junit.jupiter.api.Test;
import salon.sales.application.domain.model.customer.CustomerId;
import salon.sales.application.domain.exception.InvalidOfferStateException;
import salon.sales.application.domain.model.offer.Offer;
import salon.sales.application.domain.model.offer.OfferState;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.domain.model.order.Order;
import salon.sales.application.domain.model.order.OrderState;
import salon.common.model.Money;
import salon.common.model.OrderId;
import salon.common.model.SpecificationId;

import static org.assertj.core.api.Assertions.*;
/** UC-CRM-03: Offer acceptance and order creation */
class AcceptOfferDomainTest {

    @Test
    void shouldAcceptOfferAndAllowOrderCreation() {
        // We have an offer
        Offer offer = new Offer(new OfferId("O-1"), new CustomerId("C-1"), new SpecificationId("S-1"));
        offer.publishOffer();

        // The customer accepts the offer, and we generate an order based on it
        offer.accept();
        Order order = new Order(new OrderId("ORD-1"), offer.id(), Money.of(150000, "PLN"));

        // The offer transitions to the ACCEPTED state
        assertThat(offer.state()).isEqualTo(OfferState.ACCEPTED);

        // The order initializes correctly in the initial DRAFT_CREATED state
        assertThat(order.offerId()).isEqualTo(new OfferId("O-1"));
        assertThat(order.state()).isEqualTo(OrderState.DRAFT_CREATED);
    }

    @Test
    void shouldRejectAcceptanceWhenOfferIsAlreadyRejected() {       // Scenariusz alternatywny
        // The customer withdrew earlier and the offer was marked as rejected
        Offer offer = new Offer(new OfferId("O-2"), new CustomerId("C-2"), new SpecificationId("S-2"));
        offer.publishOffer();
        offer.reject();

        // The customer wants to change their mind, but a new one must be generated
        assertThatThrownBy(offer::accept)
                .isInstanceOf(InvalidOfferStateException.class)
                .hasMessageContaining("Cannot accept an offer that is already REJECTED");
    }
}