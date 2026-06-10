package unit.sales_and_crm_context.domainTests;

import org.junit.jupiter.api.Test;
import salon.sales.domain.model.customer.CustomerId;
import salon.sales.domain.model.offer.*;
import salon.shared.model.*;

import static org.assertj.core.api.Assertions.*;

/** UC-CRM-02: Wygenerowanie oferty proforma */
class GenerateOfferDomainTest {

    @Test
    void shouldCreateAndPublishOfferSuccessfully() {
        // Tworzymy nową ofertę dla konkretnego klienta i specyfikacji
        Offer offer = new Offer(new OfferId("O-1"), new CustomerId("C-1"), new SpecificationId("S-1"), Money.of(100000, "PLN"));

        // Handlowiec decyduje się opublikować ofertę
        offer.publishOffer();

        // Stan maszyny stanów poprawnie zmienia się na PUBLISHED
        assertThat(offer.getState()).isEqualTo(OfferState.PUBLISHED);
    }

    @Test
    void shouldRejectOfferCreationWithZeroOrNegativeValue() {
        // Próba utworzenia oferty z ujemną lub zerową kwotą
        // to złamanie podstawowej reguły biznesowej. Agregat musi to zablokować w konstruktorze.
        assertThatThrownBy(() -> new Offer(new OfferId("O-2"), new CustomerId("C-1"), new SpecificationId("S-2"), Money.of(-500, "PLN")))
                .isInstanceOf(InvalidOfferDataException.class)
                .hasMessageContaining("Offer price must be strictly positive");
    }
}