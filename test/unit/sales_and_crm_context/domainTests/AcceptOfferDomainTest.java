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
/** UC-CRM-03: Zatwierdzenie oferty i utworzenie zamówienia */
class AcceptOfferDomainTest {

    @Test
    void shouldAcceptOfferAndAllowOrderCreation() {
        // Posiadamy ofertę
        Offer offer = new Offer(new OfferId("O-1"), new CustomerId("C-1"), new SpecificationId("S-1"));
        offer.publishOffer();

        // Klient akceptuje ofertę, a my generujemy na jej podstawie zamówienie
        offer.accept();
        Order order = new Order(new OrderId("ORD-1"), offer.getId(), Money.of(150000, "PLN"));

        // Oferta przechodzi w stan ACCEPTED
        assertThat(offer.getState()).isEqualTo(OfferState.ACCEPTED);

        // Zamówienie inicjuje się poprawnie w początkowym stanie DRAFT_CREATED
        assertThat(order.getOfferId()).isEqualTo(new OfferId("O-1"));
        assertThat(order.getState()).isEqualTo(OrderState.DRAFT_CREATED);
    }

    @Test
    void shouldRejectAcceptanceWhenOfferIsAlreadyRejected() {       // Scenariusz alternatywny
        // Klient wcześniej zrezygnował i oferta została oznaczona jako odrzucona
        Offer offer = new Offer(new OfferId("O-2"), new CustomerId("C-2"), new SpecificationId("S-2"));
        offer.publishOffer();
        offer.reject();

        // Klient chce zmienić zdanie, ale trzeba wygenerować nową
        assertThatThrownBy(offer::accept)
                .isInstanceOf(InvalidOfferStateException.class)
                .hasMessageContaining("Cannot accept an offer that is already REJECTED");
    }
}