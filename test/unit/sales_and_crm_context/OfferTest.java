package unit.sales_and_crm_context;

import org.junit.jupiter.api.Test;
import salon.sales.domain.model.offer.CustomerId;
import salon.sales.domain.model.offer.Discount;
import salon.sales.domain.model.offer.DiscountLimit;
import salon.sales.domain.model.offer.Offer;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.model.offer.OfferState;
import salon.shared.model.SpecificationId;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class OfferTest {

    @Test
    void shouldApplyDiscountDirectlyWhenWithinSalespersonLimit() {
        DiscountLimit salespersonLimit = new DiscountLimit(new BigDecimal("5.00"));
        Discount requestedDiscount = new Discount(new BigDecimal("4.00"));

        Offer offer = new Offer(
                new OfferId("OFF-001"),
                new CustomerId("CUST-123"),
                new SpecificationId("SPEC-1"));

        offer.applyDiscount(requestedDiscount, salespersonLimit);

        assertThat(offer.getState()).isNotEqualTo(OfferState.PENDING_DIRECTOR_APPROVAL);
    }

    @Test
    void shouldRequireDirectorApprovalWhenDiscountExceedsLimit() {
        DiscountLimit salespersonLimit = new DiscountLimit(new BigDecimal("5.00"));
        Discount requestedDiscount = new Discount(new BigDecimal("8.00"));

        Offer offer = new Offer(
                new OfferId("OFF-002"),
                new CustomerId("CUST-124"),
                new SpecificationId("SPEC-2"));

        offer.applyDiscount(requestedDiscount, salespersonLimit);

        assertThat(offer.getState()).isEqualTo(OfferState.PENDING_DIRECTOR_APPROVAL);
    }
}
