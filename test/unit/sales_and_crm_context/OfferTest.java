import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class OfferTest {

    @Test
    void shouldApplyDiscountDirectlyWhenWithinSalespersonLimit() {
        // Arrange (Given)
        DiscountLimit salespersonLimit = new DiscountLimit(new BigDecimal("5.00")); // Limit 5%
        Discount requestedDiscount = new Discount(new BigDecimal("4.00")); // Rabat 4%

        Offer offer = new Offer(
                new OfferId("OFF-001"),
                new CustomerId("CUST-123"),
                new SpecificationId("SPEC-1")
        );

        // Act (When)
        offer.applyDiscount(requestedDiscount, salespersonLimit);

        // Assert (Then)
        // Oczekujemy, że rabat zostanie przyznany, a oferta nie wymaga zgody dyrektora
        assertThat(offer.getState()).isNotEqualTo(OfferState.PENDING_DIRECTOR_APPROVAL);
    }

    @Test
    void shouldRequireDirectorApprovalWhenDiscountExceedsLimit() {
        // Arrange (Given)
        DiscountLimit salespersonLimit = new DiscountLimit(new BigDecimal("5.00")); // Limit 5%
        Discount requestedDiscount = new Discount(new BigDecimal("8.00")); // Rabat 8% (przekracza limit!)

        Offer offer = new Offer(
                new OfferId("OFF-002"),
                new CustomerId("CUST-124"),
                new SpecificationId("SPEC-2")
        );

        // Act (When)
        offer.applyDiscount(requestedDiscount, salespersonLimit);

        // Assert (Then)
        // Oczekujemy natychmiastowej zmiany statusu na wymagający autoryzacji
        assertThat(offer.getState()).isEqualTo(OfferState.PENDING_DIRECTOR_APPROVAL);
    }
}