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
 * Aggregate Root: oferta handlowa / proforma (UC-CRM-02, UC-CRM-03).
 *
 * Model zgodny z docs/Agregate/Sales/customer-offer-order.md oraz PDF (rozdz. 3.3.4):
 *   pola:    id, customerId, specificationId, basePrice, finalPrice, validityDate, state
 *   metody:  applyDiscount(Discount), publishOffer(), accept(), reject()
 *   stany:   DRAFT -> PUBLISHED -> ACCEPTED | REJECTED (stany terminalne)
 *
 * Hermetyzacja decyzji cenowych: applyDiscount zamyka politykę rabatową wewnątrz agregatu,
 * a konstruktor pilnuje, by cena bazowa była ściśle dodatnia (reguła danych oferty).
 * Reguły ważności i konwersji również należą do agregatu (NIE do warstwy aplikacji):
 *   - accept() odrzuca ofertę po terminie ważności (OfferExpiredException),
 *   - po REJECTED/ACCEPTED oferta jest niemutowalna (OfferImmutableException),
 *   - toSnapshot() można zbudować wyłącznie z oferty ACCEPTED.
 */
public class Offer {

    /** Maksymalny rabat dopuszczalny polityką salonu (w %), pilnowany przez agregat. */
    private static final BigDecimal MAX_DISCOUNT_PERCENTAGE = new BigDecimal("20");

    private final OfferId id;
    private final CustomerId customerId;
    private final SpecificationId specificationId;

    private Money basePrice;          // może być null na etapie roboczym
    private Discount appliedDiscount; // null, dopóki nie przyznano rabatu
    private Money finalPrice;         // liczona z basePrice i rabatu
    private LocalDate validityDate;
    private OfferState state;
    private Long version;             // znacznik wersji dla blokady optymistycznej (infrastruktura)

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
        this.version = null;
    }

    /** Wariant z ceną bazową (wycena specyfikacji z Katalogu) — cena musi być ściśle dodatnia. */
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

    /** Wariant dla tożsamości klienta współdzielonej przez Shared Kernel. */
    public Offer(OfferId id, salon.common.model.CustomerId customerId, SpecificationId specificationId, Money basePrice) {
        this(id, new CustomerId(customerId.value()), specificationId, basePrice);
    }

    // Cenę bazową można ustawić na etapie roboczym (np. po wycenie ze specyfikacji).
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
     * Przyznanie rabatu (UC-CRM-02). Polityka rabatowa jest zamknięta w agregacie:
     * rabat musi mieścić się w granicach dopuszczalnych przez salon.
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

    // UC-CRM-02, krok 4: wygenerowanie dokumentu proforma i prezentacja klientowi.
    public void publishOffer() {
        if (this.state != OfferState.DRAFT) {
            throw new InvalidOfferStateException("Only a DRAFT offer can be published.");
        }
        this.state = OfferState.PUBLISHED;
    }

    /**
     * UC-CRM-03, krok 1-2: klient akceptuje warunki oferty.
     * Reguły w agregacie: tylko PUBLISHED można zaakceptować, stany terminalne są
     * niemutowalne, a oferta po terminie ważności jest odrzucana.
     */
    public void accept() {
        if (this.state == OfferState.REJECTED) {
            // Odrzucona oferta to zamknięty rozdział — klient musi dostać nową.
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

    // UC-CRM-03, scenariusz A1: klient odrzuca ofertę — stan terminalny, nic nie jest emitowane.
    // Odrzucić można zarówno zaprezentowaną (PUBLISHED), jak i roboczą (DRAFT) ofertę.
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
        // finalPrice = basePrice * (1 - rabat/100)
        BigDecimal factor = BigDecimal.ONE.subtract(
                this.appliedDiscount.percentage().movePointLeft(2));
        BigDecimal value = this.basePrice.amount().multiply(factor);
        this.finalPrice = new Money(value, this.basePrice.currency());
    }

    /**
     * Niemutowalna migawka oferty dla {@code OrderFactory} (UC-CRM-03).
     * Reguła konwersji siedzi w agregacie: zamówienie może powstać WYŁĄCZNIE
     * z oferty zaakceptowanej przez klienta.
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
