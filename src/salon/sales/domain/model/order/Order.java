package salon.sales.domain.model.order;

import salon.sales.domain.event.DepositRefundOrderedEvent;
import salon.sales.domain.event.DepositRetainedAsIncomeEvent;
import salon.sales.domain.event.OrderActivatedEvent;
import salon.sales.domain.event.OrderCancelledEvent;
import salon.sales.domain.event.OrderPlacedEvent;
import salon.sales.domain.event.VehicleHandedOverEvent;
import salon.sales.domain.model.offer.Offer;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.model.offer.OfferState;
import salon.shared.event.AbstractAggregateRoot;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root: zamówienie (UC-SPR-02, UC-SPR-03).
 *
 * Reguła kluczowa (UC-SPR-03): po WYDANIU pojazdu zamówienia nie wolno już anulować —
 * cancelOrder rzuca wtedy wyjątkiem (blokada operacji), bo dalej idzie ścieżka reklamacji/serwisu.
 *
 * Zdarzenia domenowe: to agregat decyduje (na podstawie swojego stanu / powodu anulacji),
 * jakie zdarzenie wyemitować. Warstwa aplikacji tylko je ściąga i publikuje.
 */
public class Order extends AbstractAggregateRoot {

    private final OrderId id;
    private final OfferId sourceOfferId;

    private Money requiredDeposit;             // może dojść później (z oferty)
    private String signatureRef;               // referencja podpisu umowy
    private OrderState state;
    private CancellationReason cancellationReason;

    public Order(OrderId id, OfferId sourceOfferId) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (sourceOfferId == null) {
            throw new IllegalArgumentException("sourceOfferId must not be null.");
        }
        this.id = id;
        this.sourceOfferId = sourceOfferId;
        this.requiredDeposit = null;
        this.signatureRef = null;
        this.state = OrderState.DRAFT_CREATED;
        this.cancellationReason = CancellationReason.NONE;
    }

    // UC-SPR-02: zamówienie powstaje z opublikowanej oferty.
    public static Order createFromOffer(Offer offer) {
        if (offer == null) {
            throw new IllegalArgumentException("offer must not be null.");
        }
        if (offer.getState() != OfferState.PUBLISHED) {
            throw new IllegalStateException("Order can only be created from a PUBLISHED offer.");
        }
        Order order = new Order(OrderId.generate(), offer.getId());
        order.requiredDeposit = offer.getFinalPrice(); // może być null, jeśli nie wyceniono
        return order;
    }

    // UC-SPR-02: podpis umowy -> zamówienie czeka na zadatek.
    public void confirmSignature(String signatureRef) {
        if (signatureRef == null || signatureRef.isBlank()) {
            throw new IllegalArgumentException("signatureRef must not be blank.");
        }
        if (this.state != OrderState.DRAFT_CREATED) {
            throw new IllegalStateException("Only a freshly created order can be signed.");
        }
        this.signatureRef = signatureRef;
        this.state = OrderState.PENDING_PAYMENT;
        // Po podpisie zamówienie jest formalnie złożone — ogłaszamy to światu (UC-SPR-02).
        registerEvent(new OrderPlacedEvent(
                UUID.randomUUID(), this.id.value(), Instant.now()));
    }

    // UC-SPR-02, krok 5: zadatek zaksięgowany (sygnał z Rozliczeń) -> uruchamiamy realizację.
    public void activate() {
        if (this.state != OrderState.PENDING_PAYMENT) {
            throw new IllegalStateException("Only an order pending payment can be activated.");
        }
        this.state = OrderState.IN_PROGRESS;
        registerEvent(new OrderActivatedEvent(
                UUID.randomUUID(), this.id.value(), Instant.now()));
    }

    /**
     * UC-SPR-03: anulowanie zamówienia.
     * Jeśli pojazd został już wydany — blokujemy operację (wyjątek).
     * Wina klienta -> zatrzymujemy zadatek (DepositRetainedAsIncomeEvent),
     * w przeciwnym razie -> zlecamy zwrot (DepositRefundOrderedEvent).
     */
    public void cancelOrder(CancellationReason reason, boolean isHandedOver) {
        if (reason == null) {
            throw new IllegalArgumentException("reason must not be null.");
        }
        if (isHandedOver) {
            throw new IllegalStateException(
                    "Cannot cancel order after the vehicle has been handed over.");
        }
        if (this.state == OrderState.CANCELLED || this.state == OrderState.COMPLETED) {
            throw new IllegalStateException("Order is already " + this.state + ".");
        }
        this.cancellationReason = reason;
        this.state = OrderState.CANCELLED;

        // Najpierw ogłaszamy sam fakt anulowania (wraz z powodem) — to zdarzenie nadrzędne (UC-SPR-03).
        registerEvent(new OrderCancelledEvent(
                UUID.randomUUID(), this.id.value(), reason.name(), Instant.now()));

        // Następnie zdarzenie-polecenie dla Rozliczeń: jak potraktować zadatek.
        if (reason == CancellationReason.CLIENT_FAULT) {
            registerEvent(new DepositRetainedAsIncomeEvent(
                    UUID.randomUUID(), this.id.value(), Instant.now()));
        } else {
            registerEvent(new DepositRefundOrderedEvent(
                    UUID.randomUUID(), this.id.value(), Instant.now()));
        }
    }

    /**
     * UC-SPR-08: wydanie pojazdu klientowi — finalny krok zamówienia.
     * Zamknięcie zamówienia (COMPLETED) i ogłoszenie zdarzenia o wydaniu auta,
     * którego nasłuchują Rozliczenia (domknięcie salda) oraz obsługa posprzedażowa.
     */
    public void completeHandover() {
        if (this.state == OrderState.CANCELLED) {
            throw new IllegalStateException("A cancelled order cannot be handed over.");
        }
        if (this.state == OrderState.COMPLETED) {
            throw new IllegalStateException("Order is already completed.");
        }
        this.state = OrderState.COMPLETED;
        registerEvent(new VehicleHandedOverEvent(
                UUID.randomUUID(), this.id.value(), Instant.now()));
    }

    public OrderId getId() {
        return this.id;
    }

    public OfferId getSourceOfferId() {
        return this.sourceOfferId;
    }

    public Money getRequiredDeposit() {
        return this.requiredDeposit;
    }

    public OrderState getState() {
        return this.state;
    }

    public CancellationReason getCancellationReason() {
        return this.cancellationReason;
    }
}
