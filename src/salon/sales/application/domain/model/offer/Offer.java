package salon.sales.application.domain.model.offer;

import salon.sales.application.domain.exception.InvalidOfferStateException;
import salon.sales.application.domain.exception.OfferExpiredException;
import salon.sales.application.domain.exception.OfferImmutableException;
import salon.sales.application.domain.model.customer.CustomerId;
import salon.common.model.Money;
import salon.common.model.SpecificationId;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Aggregate Root: a commercial offer / proforma (UC-CRM-02, UC-CRM-03).
 *
 * Model consistent with docs/Agregate/Sales/customer-offer-order.md and the PDF (chapter 3.3.4):
 *   pola:    id, customerId, specificationId, basePrice, finalPrice, validityDate, state
 *   metody:  applyDiscount(Discount), publishOffer(), accept(), reject()
 *   states:  DRAFT -> PUBLISHED -> ACCEPTED | REJECTED (terminal states)
 *
 * Encapsulation of pricing decisions: applyDiscount encloses the discount policy inside the aggregate,
 * and the constructor ensures the base price is strictly positive (the offer data rule).
 * The validity and conversion rules also belong to the aggregate (NOT to the application layer):
 *   - accept() rejects an offer past its validity date (OfferExpiredException),
 *   - after REJECTED/ACCEPTED the offer is immutable (OfferImmutableException),
 *   - toSnapshot() can be built only from an ACCEPTED offer.
 */
public class Offer {

    /** The maximum discount allowed by the dealership policy (in %), enforced by the aggregate. */
    private static final BigDecimal MAX_DISCOUNT_PERCENTAGE = new BigDecimal("20");

    private final OfferId id;
    private final CustomerId customerId;
    private final SpecificationId specificationId;

    private Money basePrice;          // may be null at the draft stage
    private Discount appliedDiscount; // null until a discount is granted
    private Money finalPrice;         // computed from basePrice and the discount
    private LocalDate validityDate;
    private OfferState state;
    private Long version;             // version marker for optimistic locking (infrastructure)

    public Offer(OfferId id, CustomerId customerId, SpecificationId specificationId) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null.");
        }
        if (specificationId == null) {
            throw new IllegalArgumentException("specificationId must not be null.");
        }
        this.id = id;
        this.customerId = customerId;
        this.specificationId = specificationId;
        this.basePrice = null;
        this.appliedDiscount = null;
        this.finalPrice = null;
        this.validityDate = LocalDate.now().plusDays(14); // the offer is valid for 14 days
        this.state = OfferState.DRAFT;
        this.version = null;
    }

    /** Variant with a base price (specification pricing from the Catalog) — the price must be strictly positive. */
    public Offer(OfferId id, CustomerId customerId, SpecificationId specificationId, Money basePrice) {
        this(id, customerId, specificationId);
        if (basePrice == null) {
            throw new IllegalArgumentException("basePrice must not be null.");
        }
        if (basePrice.amount().signum() <= 0) {
            throw new InvalidOfferDataException("Offer price must be strictly positive");
        }
        this.basePrice = basePrice;
        recomputeFinalPrice();
    }

    /** Variant for a customer identity shared through the Shared Kernel. */
    public Offer(OfferId id, salon.common.model.CustomerId customerId, SpecificationId specificationId, Money basePrice) {
        this(id, new CustomerId(customerId.value()), specificationId, basePrice);
    }

    // The base price can be set at the draft stage (e.g. after pricing from the specification).
    public void changeBasePrice(Money basePrice) {
        if (basePrice == null) {
            throw new IllegalArgumentException("basePrice must not be null.");
        }
        if (basePrice.amount().signum() <= 0) {
            throw new InvalidOfferDataException("Offer price must be strictly positive");
        }
        if (this.state != OfferState.DRAFT) {
            throw new InvalidOfferStateException("Base price can only be set on a DRAFT offer.");
        }
        this.basePrice = basePrice;
        recomputeFinalPrice();
    }

    /**
     * Granting a discount (UC-CRM-02). The discount policy is enclosed in the aggregate:
     * the discount must stay within the limits allowed by the dealership.
     */
    public void applyDiscount(Discount discount) {
        if (discount == null) {
            throw new IllegalArgumentException("discount must not be null.");
        }
        if (this.state != OfferState.DRAFT) {
            throw new InvalidOfferStateException("Discount can only be applied to a DRAFT offer.");
        }
        if (discount.percentage().compareTo(MAX_DISCOUNT_PERCENTAGE) > 0) {
            throw new IllegalArgumentException(
                    "Discount " + discount.percentage() + "% exceeds the dealership policy limit of "
                            + MAX_DISCOUNT_PERCENTAGE + "%.");
        }
        this.appliedDiscount = discount;
        recomputeFinalPrice();
    }

    // UC-CRM-02, step 4: generating the proforma document and presenting it to the customer.
    public void publishOffer() {
        if (this.state != OfferState.DRAFT) {
            throw new InvalidOfferStateException("Only a DRAFT offer can be published.");
        }
        this.state = OfferState.PUBLISHED;
    }

    /**
     * UC-CRM-03, steps 1-2: the customer accepts the offer terms.
     * Rules in the aggregate: only a PUBLISHED offer can be accepted, terminal states are
     * immutable, and an offer past its validity date is rejected.
     */
    public void accept() {
        if (this.state == OfferState.REJECTED) {
            // A rejected offer is a closed chapter — the customer must get a new one.
            throw new OfferImmutableException(
                    "Cannot change state of a REJECTED offer. "
                            + "Cannot accept an offer that is already REJECTED.");
        }
        if (this.state != OfferState.PUBLISHED) {
            throw new InvalidOfferStateException("Only PUBLISHED offers can be accepted");
        }
        if (this.validityDate.isBefore(LocalDate.now())) {
            throw new OfferExpiredException("Offer " + this.id.value() + " has expired.");
        }
        this.state = OfferState.ACCEPTED;
    }

    // UC-CRM-03, scenario A1: the customer rejects the offer — a terminal state, nothing is emitted.
    // Both a presented (PUBLISHED) and a draft (DRAFT) offer can be rejected.
    public void reject() {
        if (this.state == OfferState.ACCEPTED || this.state == OfferState.REJECTED) {
            throw new OfferImmutableException(
                    "Cannot change state of a " + this.state + " offer.");
        }
        this.state = OfferState.REJECTED;
    }

    private void recomputeFinalPrice() {
        if (this.basePrice == null) {
            return;
        }
        if (this.appliedDiscount == null) {
            this.finalPrice = this.basePrice;
            return;
        }
        // finalPrice = basePrice * (1 - discount/100)
        BigDecimal factor = BigDecimal.ONE.subtract(
                this.appliedDiscount.percentage().movePointLeft(2));
        BigDecimal value = this.basePrice.amount().multiply(factor);
        this.finalPrice = new Money(value, this.basePrice.currency());
    }

    /**
     * Immutable snapshot of the offer for {@code OrderFactory} (UC-CRM-03).
     * The conversion rule sits in the aggregate: an order can be created ONLY
     * from an offer accepted by the customer.
     */
    public OfferSnapshot toSnapshot() {
        if (this.state != OfferState.ACCEPTED) {
            throw new InvalidOfferStateException("Order can only be created from an ACCEPTED offer.");
        }
        return new OfferSnapshot(this.id, this.customerId, this.specificationId, this.finalPrice);
    }

    public OfferId id() {
        return this.id;
    }

    public CustomerId customerId() {
        return this.customerId;
    }

    public SpecificationId specificationId() {
        return this.specificationId;
    }

    public Money basePrice() {
        return this.basePrice;
    }

    public Discount appliedDiscount() {
        return this.appliedDiscount;
    }

    public Money finalPrice() {
        return this.finalPrice;
    }

    public LocalDate validityDate() {
        return this.validityDate;
    }

    public OfferState state() {
        return this.state;
    }
}
