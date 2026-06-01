package salon.sales.domain.model.offer;

import salon.shared.model.Money;
import salon.shared.model.SpecificationId;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Aggregate Root: oferta handlowa (UC-SPR-01).
 *
 * Reguła kluczowa (A1): rabat powyżej limitu Handlowca NIE jest przyznawany od ręki —
 * oferta natychmiast przechodzi w stan PENDING_DIRECTOR_APPROVAL (wymaga autoryzacji Dyrektora).
 *
 * basePrice jest opcjonalna (może dojść później); finalPrice liczymy tylko gdy znamy basePrice.
 */
public class Offer {

    private final OfferId id;
    private final CustomerId customerId;
    private final SpecificationId specificationId;

    private Money basePrice;          // może być null na etapie roboczym
    private Discount appliedDiscount; // null, dopóki nie przyznano rabatu
    private Money finalPrice;         // liczona z basePrice i rabatu
    private LocalDate validityDate;
    private OfferState state;

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
        this.validityDate = LocalDate.now().plusDays(14); // oferta ważna 14 dni
        this.state = OfferState.DRAFT;
    }

    // Cenę bazową można ustawić raz (np. po wycenie ze specyfikacji).
    public void setBasePrice(Money basePrice) {
        if (basePrice == null) {
            throw new IllegalArgumentException("basePrice must not be null.");
        }
        this.basePrice = basePrice;
        recomputeFinalPrice();
    }

    /**
     * UC-SPR-01: przyznanie rabatu.
     * Jeśli żądany rabat > limit Handlowca -> blokujemy i wymagamy zgody Dyrektora (A1).
     * W przeciwnym razie rabat jest przyznany od ręki.
     */
    public void applyDiscount(Discount discount, DiscountLimit limit) {
        if (discount == null) {
            throw new IllegalArgumentException("discount must not be null.");
        }
        if (limit == null) {
            throw new IllegalArgumentException("limit must not be null.");
        }
        if (this.state != OfferState.DRAFT) {
            throw new IllegalStateException("Discount can only be applied to a DRAFT offer.");
        }

        if (discount.percentage().compareTo(limit.maxAllowed()) > 0) {
            // Rabat przekracza limit -> wymaga autoryzacji Dyrektora.
            this.appliedDiscount = discount;
            this.state = OfferState.PENDING_DIRECTOR_APPROVAL;
        } else {
            // Rabat w granicach limitu -> przyznany od ręki.
            this.appliedDiscount = discount;
            recomputeFinalPrice();
        }
    }

    // UC-SPR-01, A1: Dyrektor zatwierdza rabat -> oferta wraca do obiegu (DRAFT) i można ją wysłać.
    public void approveDiscountByDirector() {
        if (this.state != OfferState.PENDING_DIRECTOR_APPROVAL) {
            throw new IllegalStateException("Only an offer pending director approval can be approved.");
        }
        recomputeFinalPrice();
        this.state = OfferState.DRAFT;
    }

    // Wysłanie oferty klientowi.
    public void publish() {
        if (this.state == OfferState.PENDING_DIRECTOR_APPROVAL) {
            throw new IllegalStateException("Offer needs director approval before it can be published.");
        }
        if (this.state != OfferState.DRAFT) {
            throw new IllegalStateException("Only a DRAFT offer can be published.");
        }
        this.state = OfferState.PUBLISHED;
    }

    // Zamiana na zamówienie (UC-SPR-02).
    public void markAsConverted() {
        if (this.state != OfferState.PUBLISHED) {
            throw new IllegalStateException("Only a PUBLISHED offer can be converted to an order.");
        }
        this.state = OfferState.CONVERTED;
    }

    private void recomputeFinalPrice() {
        if (this.basePrice == null || this.appliedDiscount == null) {
            return;
        }
        // finalPrice = basePrice * (1 - rabat/100)
        BigDecimal factor = BigDecimal.ONE.subtract(
                this.appliedDiscount.percentage().movePointLeft(2));
        BigDecimal value = this.basePrice.amount().multiply(factor);
        this.finalPrice = new Money(value, this.basePrice.currency());
    }

    public OfferId getId() {
        return this.id;
    }

    public CustomerId getCustomerId() {
        return this.customerId;
    }

    public SpecificationId getSpecificationId() {
        return this.specificationId;
    }

    public Money getBasePrice() {
        return this.basePrice;
    }

    public Discount getAppliedDiscount() {
        return this.appliedDiscount;
    }

    public Money getFinalPrice() {
        return this.finalPrice;
    }

    public LocalDate getValidityDate() {
        return this.validityDate;
    }

    public OfferState getState() {
        return this.state;
    }
}
