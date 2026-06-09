package integration.driven_adapters.database_adapter.sales;

import salon.sales.infrastructure.persistence.OfferDatabaseAdapter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.Import;
import salon.sales.domain.model.offer.Offer;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.model.offer.OfferState;
import salon.sales.domain.model.offer.Discount;
import salon.sales.domain.model.offer.DiscountLimit;
import salon.sales.domain.model.customer.CustomerId;
import salon.shared.model.SpecificationId;
import salon.shared.model.Money;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@EntityScan("salon")
@EnableJpaRepositories("salon")
@DataJpaTest
@Import(OfferDatabaseAdapter.class)
class OfferDatabaseAdapterTest {

    @Autowired
    private OfferDatabaseAdapter adapter;

    // 1. ZAPIS, ODCZYT I MAPOWANIE: Weryfikacja rabatu i zmiany stanu
    @Test
    void shouldSaveAndRetrieveOfferWithDiscountAndStateMapped() {
        // Arrange
        OfferId offerId = new OfferId("OFF-2026-001");
        CustomerId customerId = new CustomerId("CUST-123");
        SpecificationId specId = new SpecificationId("SPEC-999");
        Money basePrice = Money.of(new BigDecimal("200000.00"), "PLN");

        Offer offer = new Offer(offerId, customerId, specId);
        offer.setBasePrice(basePrice);

        // Zgodnie z analizą: żądany rabat 10% przekracza limit sprzedawcy (5%),
        // więc agregat blokuje go i przenosi ofertę do akceptacji Dyrektora (UC-SPR-01 A1).
        Discount requestedDiscount = new Discount(new BigDecimal("10.00"));
        DiscountLimit limit = new DiscountLimit(new BigDecimal("5.00"));
        offer.applyDiscount(requestedDiscount, limit);

        // Act - Zapis i odczyt z bazy (wymusza pełne mapowanie tam i z powrotem)
        adapter.save(offer);
        Optional<Offer> retrievedOffer = adapter.findById(offerId);

        // Assert
        assertThat(retrievedOffer).isPresent();
        Offer retrieved = retrievedOffer.get();

        // Weryfikacja referencji (rozłączne identyfikatory)
        assertThat(retrieved.getId()).isEqualTo(offerId);
        assertThat(retrieved.getCustomerId()).isEqualTo(customerId);
        assertThat(retrieved.getSpecificationId()).isEqualTo(specId);

        // Weryfikacja ceny bazowej
        assertThat(retrieved.getBasePrice().getAmount()).isEqualByComparingTo("200000.00");

        // Weryfikacja zapamiętanego rabatu
        assertThat(retrieved.getAppliedDiscount().percentage()).isEqualByComparingTo("10.00");

        // Krytyczna weryfikacja logiki biznesowej zmapowanej na bazę danych:
        // rabat ponad limit -> stan oczekiwania na akceptację Dyrektora; cena końcowa NIE jest
        // jeszcze przeliczana (dopiero po zatwierdzeniu rabatu przez Dyrektora).
        assertThat(retrieved.getState()).isEqualTo(OfferState.PENDING_DIRECTOR_APPROVAL);
        assertThat(retrieved.getFinalPrice()).isNull();
    }

    // 2. BRAK DANYCH
    @Test
    void shouldReturnEmptyOptionalWhenOfferDoesNotExist() {
        // Act
        Optional<Offer> result = adapter.findById(new OfferId("OFF-UNKNOWN"));

        // Assert
        assertThat(result).isEmpty();
    }
}
