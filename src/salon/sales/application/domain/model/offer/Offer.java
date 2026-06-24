package salon.sales.application.domain.model.offer;

import salon.common.event.AbstractAggregateRoot;
import salon.common.model.Money;
import salon.common.model.SpecificationId;
import salon.sales.application.domain.exception.InvalidOfferStateException;
import salon.sales.application.domain.exception.OfferExpiredException;
import salon.sales.application.domain.exception.OfferImmutableException;
import salon.sales.application.domain.model.customer.CustomerId;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Offer extends AbstractAggregateRoot {

    
    private static final BigDecimal MAX_DISCOUNT_PERCENT = BigDecimal.valueOf(20);

    private final OfferId id;
    private final CustomerId customerId;
    private final SpecificationId specificationId;
    private final Money basePrice;
    private Money finalPrice;
    private OfferState state;
    private LocalDate validityDate;
    private Long version; 

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
        
        if (basePrice.amount().signum() <= 0) {
            throw new InvalidOfferDataException("Offer price must be strictly positive");
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
        if (this.state == OfferState.PUBLISHED) {
            if (this.validityDate.isBefore(LocalDate.now())) {
                throw new OfferExpiredException("Offer " + this.id + " expired on " + this.validityDate + ".");
            }
            this.state = OfferState.ACCEPTED;
            return;
        }
        
        if (this.state == OfferState.REJECTED) {
            throw new OfferImmutableException(
                    "Cannot accept an offer that is already REJECTED. Cannot change state of a REJECTED offer.");
        }
        if (this.state == OfferState.ACCEPTED) {
            throw new OfferImmutableException(
                    "Cannot accept an offer that is already ACCEPTED. Cannot change state of an ACCEPTED offer.");
        }
        
        throw new InvalidOfferStateException(
                "Only PUBLISHED offers can be accepted (current: " + this.state + ").");
    }

    
    public void reject() {
        if (this.state != OfferState.DRAFT && this.state != OfferState.PUBLISHED) {
            throw new OfferImmutableException(
                    "Cannot change state of a " + this.state + " offer.");
        }
        this.state = OfferState.REJECTED;
    }

    

    public OfferId id() {
        return id;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public SpecificationId specificationId() {
        return specificationId;
    }

    public Money basePrice() {
        return basePrice;
    }

    public Money finalPrice() {
        return finalPrice;
    }

    public OfferState state() {
        return state;
    }

    public LocalDate validityDate() {
        return validityDate;
    }

    
    public OfferSnapshot toSnapshot() {
        return new OfferSnapshot(this.id, this.customerId, this.specificationId, this.finalPrice);
    }
    

    public Long version() {
        return version;
    }

    
    public static Offer reconstitute(OfferId id, CustomerId customerId, SpecificationId specificationId,
                                     Money basePrice, Money finalPrice, OfferState state,
                                     LocalDate validityDate, Long version) {
        Offer offer = new Offer(id, customerId, specificationId, basePrice);
        offer.finalPrice = finalPrice;
        offer.state = state;
        offer.validityDate = validityDate;
        offer.version = version;
        return offer;
    }
}
