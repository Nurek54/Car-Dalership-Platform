package salon.sales.application.domain.model.offer;

import salon.common.model.Money;
import salon.common.model.SpecificationId;
import salon.sales.application.domain.exception.InvalidOfferStateException;
import salon.sales.application.domain.exception.OfferExpiredException;
import salon.sales.application.domain.model.customer.CustomerId;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Offer {

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

    public void publishOffer() {
        if (this.state != OfferState.DRAFT) {
            throw new InvalidOfferStateException(
                    "Only a DRAFT offer can be published (current: " + this.state + ").");
        }
        this.state = OfferState.PUBLISHED;
    }

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
