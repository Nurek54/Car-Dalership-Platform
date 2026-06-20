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
        // Posiadamy poprawne dane wejściowe dla nowej oferty
        CustomerId customerId = new CustomerId("CUST-1");
        SpecificationId specId = new SpecificationId("SPEC-1");
        Money basePrice = Money.of(150000, "PLN");

        Offer offer = offerFactory.createOffer(customerId, specId, basePrice);

        assertThat(offer).isNotNull();
        assertThat(offer.getId()).isNotNull(); // Fabryka sama generuje ID
        assertThat(offer.getState()).isEqualTo(OfferState.DRAFT);
        assertThat(offer.getFinalPrice()).isEqualTo(basePrice);
    }

    @Test
    void shouldRejectCreatingOfferForNegativePrice() {
        // Błędna, ujemna kwota z cennika
        CustomerId customerId = new CustomerId("CUST-1");
        SpecificationId specId = new SpecificationId("SPEC-1");
        Money invalidPrice = Money.of(-100, "PLN");

        // Fabryka odrzuca próbę utworzenia oferty
        assertThatThrownBy(() -> offerFactory.createOffer(customerId, specId, invalidPrice))
                .isInstanceOf(InvalidOfferDataException.class)
                .hasMessageContaining("Cannot create offer with zero or negative base price");
    }
}