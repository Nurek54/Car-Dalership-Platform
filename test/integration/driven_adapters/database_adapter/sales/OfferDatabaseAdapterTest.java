package integration.driven_adapters.database_adapter.sales;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import salon.sales.domain.model.Offer;
import salon.sales.domain.model.OfferId;
import salon.sales.domain.model.OfferState;
import salon.sales.domain.model.Discount;
import salon.sales.domain.model.DiscountLimit;
import salon.shared.model.CustomerId;
import salon.shared.model.SpecificationId;
import salon.shared.model.Money;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(OfferDatabaseAdapter.class)
class OfferDatabaseAdapterTest {

    @Autowired
    private OfferDatabaseAdapter adapter;

    // 1. ZAPIS, ODCZYT I MAPOWANIE: Weryfikacja rabatów i zmiany stanów
    @Test
    void shouldSaveAndRetrieveOfferWithDiscountAndStateMapped() {
        // Arrange
        OfferId offerId = new OfferId("OFF-2026-001");
        CustomerId customerId = new CustomerId("CUST-123");
        SpecificationId specId = new SpecificationId("SPEC-999");
        Money basePrice = Money.of(new BigDecimal("200000.00"), "PLN");

        Offer offer = new Offer(offerId, customerId, specId, basePrice, new Date());

        // Zgodnie z analizą: aplikujemy rabat 10%, ale limit sprzedawcy to 5%
        Discount requestedDiscount = new Discount(new BigDecimal("10.00"));
        DiscountLimit limit = new DiscountLimit(new BigDecimal("5.00"));

        // Metoda applyDiscount powinna wykryć przekroczenie i zmienić stan na PENDING_DIRECTOR_APPROVAL
        offer.applyDiscount(requestedDiscount, limit);

        // Act - Zapis w bazie danych
        adapter.save(offer);

        // Odczyt z bazy
        Optional<Offer> retrievedOffer = adapter.findById(offerId);

        // Assert
        assertThat(retrievedOffer).isPresent();
        Offer retrieved = retrievedOffer.get();

        // Weryfikacja referencji (rozłączne identyfikatory)
        assertThat(retrieved.getId()).isEqualTo(offerId);
        assertThat(retrieved.getCustomerId()).isEqualTo(customerId);
        assertThat(retrieved.getSpecificationId()).isEqualTo(specId);

        // Weryfikacja cen
        assertThat(retrieved.getBasePrice().getAmount()).isEqualByComparingTo("200000.00");
        // Ostateczna cena powinna zostać przeliczona (200 000 - 10% = 180 000)
        assertThat(retrieved.getFinalPrice().getAmount()).isEqualByComparingTo("180000.00");

        // Weryfikacja rabatu
        assertThat(retrieved.getAppliedDiscount().getPercentage()).isEqualByComparingTo("10.00");

        // Krytyczna weryfikacja logiki biznesowej zmapowanej na bazę danych
        assertThat(retrieved.getState()).isEqualTo(OfferState.PENDING_DIRECTOR_APPROVAL);
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