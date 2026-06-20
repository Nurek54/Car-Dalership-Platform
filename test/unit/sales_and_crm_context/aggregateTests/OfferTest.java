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
        // Nowo utworzona oferta, zawsze powstaje jako DRAFT
        Offer offer = new Offer(
                new OfferId("O-200"),
                new CustomerId("C-001"),
                new SpecificationId("SPEC-1")
        );

        // Handlowiec kończy uzupełniać ofertę i ją publikuje
        offer.publishOffer();

        // Stan zmienia się na PUBLISHED
        assertThat(offer.getState()).isEqualTo(OfferState.PUBLISHED);
    }

    @Test
    void shouldFailToAcceptOfferThatWasNeverPublished() {
        Offer offer = new Offer(
                new OfferId("O-201"),
                new CustomerId("C-001"),
                new SpecificationId("SPEC-1")
        );

        // Próba akceptacji przez klienta musi zostać zablokowana
        // Zabezpiecza to przed sytuacją, w której klient akceptuje warunki będące "w trakcie edycji"
        assertThatThrownBy(() -> offer.accept())
                .isInstanceOf(InvalidOfferStateException.class)
                .hasMessageContaining("Only PUBLISHED offers can be accepted");

        // Stan pozostaje niezmieniony
        assertThat(offer.getState()).isEqualTo(OfferState.DRAFT);
    }

    @Test
    void shouldSuccessfullyRejectPublishedOffer() {
        Offer offer = new Offer(
                new OfferId("O-202"),
                new CustomerId("C-001"),
                new SpecificationId("SPEC-1"),
                Money.of(120000, "PLN")
        );
        offer.publishOffer(); // Stan: PUBLISHED

        // Klient odrzuca ofertę
        offer.reject();

        // Stan zmienia się na REJECTED
        assertThat(offer.getState()).isEqualTo(OfferState.REJECTED);
    }

    @Test
    void shouldPreventStateMutationAfterRejection() {
        // Odrzucona oferta
        Offer offer = new Offer(
                new OfferId("O-203"),
                new CustomerId("C-001"),
                new SpecificationId("SPEC-1")
        );
        offer.publishOffer();
        offer.reject();

        // Klient dzwoni, że jednak zmienił zdanie.
        // Agregat musi to zablokować - odrzucona oferta to zamknięty rozdział, trzeba zrobić nową
        assertThatThrownBy(() -> offer.accept())
                .isInstanceOf(OfferImmutableException.class)
                .hasMessageContaining("Cannot change state of a REJECTED offer");
    }
}