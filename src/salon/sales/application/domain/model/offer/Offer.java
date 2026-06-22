package salon.sales.application.domain.model.offer;

import salon.common.model.Money;
import salon.common.model.SpecificationId;
import salon.sales.application.domain.exception.InvalidOfferStateException;
import salon.sales.application.domain.exception.OfferExpiredException;
import salon.sales.application.domain.model.customer.CustomerId;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * AGGREGATE ROOT (Figure 23) — the proforma Offer.
 *
 * Owns the pricing decision (base/final price, discount policy) and the offer lifecycle
 * (DRAFT -> PUBLISHED -> ACCEPTED/REJECTED). The specification id and base price come from the
 * Catalog's SpecificationCompleted event (UC-CRM-02); the dealership discount policy caps the discount.
 */
public class Offer {

    /** Dealership policy: a single salesperson may grant at most this discount without approval. */
    private static final BigDecimal MAX_DISCOUNT_PERCENT = BigDecimal.valueOf(20);

    private final OfferId id;
    private final CustomerId customerId;
    private final SpecificationId specificationId;
    private final Money basePrice;
    private Money finalPrice;
    private OfferState state;
    private LocalDate validityDate;

    public Offer(OfferId id, CustomerId customerId, SpecificationId specificationId, Money basePrice) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null.");
        }
        if (specificationId == null) {
            throw new IllegalArgumentException("specificationId must not be null.");
        }
        if (basePrice == null) {
            throw new IllegalArgumentException("basePrice must not be null.");
        }
        this.id = id;
        this.customerId = customerId;
        this.specificationId = specificationId;
        this.basePrice = basePrice;
        this.finalPrice = basePrice;
        this.state = OfferState.DRAFT;
        this.validityDate = LocalDate.now().plusDays(14);
    }

    /** Applies a percentage discount to the base price; bounded by the dealership policy. */
    public void applyDiscount(Discount discount) {
        if (discount == null) {
            throw new IllegalArgumentException("discount must not be null.");
        }
        if (this.state != OfferState.DRAFT) {
            throw new InvalidOfferStateException(
                    "A discount can be applied only to a DRAFT offer (current: " + this.state + ").");
        }
        if (discount.percentage().compareTo(MAX_DISCOUNT_PERCENT) > 0) {
            throw new IllegalArgumentException(
                    "Discount " + discount.percentage() + "% exceeds the dealership policy of "
                            + MAX_DISCOUNT_PERCENT + "%.");
        }
        BigDecimal factor = BigDecimal.ONE.subtract(
                discount.percentage().divide(BigDecimal.valueOf(100)));
        this.finalPrice = Money.of(this.basePrice.amount().multiply(factor), this.basePrice.currency());
    }

    /** UC-CRM-02: publishes the proforma offer to the customer. */
    public void publishOffer() {
        if (this.state != OfferState.DRAFT) {
            throw new InvalidOfferStateException(
                    "Only a DRAFT offer can be published (current: " + this.state + ").");
        }
        this.state = OfferState.PUBLISHED;
    }

    /** UC-CRM-03: the customer accepts a still-valid published offer. */
    public void accept() {
        if (this.state != OfferState.PUBLISHED) {
            throw new InvalidOfferStateException(
                    "Only a PUBLISHED offer can be accepted (current: " + this.state + ").");
        }
        if (this.validityDate.isBefore(LocalDate.now())) {
            throw new OfferExpiredException("Offer " + this.id + " expired on " + this.validityDate + ".");
        }
        this.state = OfferState.ACCEPTED;
    }

    /** UC-CRM-03 / A1: the customer declines the offer. */
    public void reject() {
        if (this.state != OfferState.DRAFT && this.state != OfferState.PUBLISHED) {
            throw new InvalidOfferStateException(
                    "Only a DRAFT or PUBLISHED offer can be rejected (current: " + this.state + ").");
        }
        this.state = OfferState.REJECTED;
    }

    public OfferId getId() {
        return id;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public SpecificationId getSpecificationId() {
        return specificationId;
    }

    public Money getBasePrice() {
        return basePrice;
    }

    public Money getFinalPrice() {
        return finalPrice;
    }

    public OfferState getState() {
        return state;
    }

    public LocalDate getValidityDate() {
        return validityDate;
    }
}
