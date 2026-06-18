package salon.sales.application.domain.model.order;

import salon.sales.application.domain.event.BankTransferDeclaredEvent;
import salon.sales.application.domain.event.OrderActivatedEvent;
import salon.sales.application.domain.event.OrderCancelledEvent;
import salon.sales.application.domain.event.OrderCompletedEvent;
import salon.sales.application.domain.event.OrderPlacedEvent;
import salon.sales.application.domain.event.OrderReadyForHandoverEvent;
import salon.sales.application.domain.event.VehicleHandedOverEvent;
import salon.sales.application.domain.model.offer.OfferId;
import salon.common.event.AbstractAggregateRoot;
import salon.common.model.Money;
import salon.common.model.OrderId;
import salon.common.model.SpecificationId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Aggregate Root: zamówienie (UC-CRM-03, UC-CRM-04, UC-CRM-05).
 *
 * Model zgodny z docs/Agregate/Sales/customer-offer-order.md i docs/Agregate/Sales/order.md:
 *   pola:    id, sourceOfferId, requiredDeposit, paymentMethod, paymentStatus,
 *            handoverDate, state (+ vehicleId przypisany przy gotowości do wydania)
 *   metody:  declarePaymentMethod, activate, markAsReadyForHandover, scheduleHandover,
 *            confirmHandover, revertToReadyForHandover, cancelOrder
 *   stany:   DRAFT_CREATED/DRAFT -> IN_PROGRESS -> READY_FOR_HANDOVER
 *            -> HANDOVER_SCHEDULED -> COMPLETED (CANCELLED — rezygnacja klienta)
 *
 * Polityka płatności: declarePaymentMethod() hermetyzuje decyzję, które zdarzenie
 * opuści agregat (BankTransferDeclaredEvent / FinancingRequestedEvent).
 * Mechanizm kompensacyjny (saga): revertToReadyForHandover() pozwala bezpiecznie cofnąć
 * zamówienie po odmowie wyksięgowania pojazdu przez Inwentarz (UC-CRM-05, A1) —
 * czyszcząc ustaloną datę wydania.
 */
public class Order extends AbstractAggregateRoot {

    private final OrderId id;
    private final OfferId sourceOfferId;
    private final SpecificationId specificationId; // specyfikacja z oferty (event-carried state transfer do Inwentarza)
    private final Money requiredDeposit;   // wynegocjowana wartość kontraktu (może być null)

    private PaymentMethod paymentMethod;   // zadeklarowana forma płatności (UC-CRM-03, krok 4)
    private PaymentStatus paymentStatus;   // synchronizowany ze zdarzeń Rozliczeń
    private LocalDate handoverDate;        // ustalony termin odbioru (UC-CRM-04)
    private String vehicleId;              // VIN przypisany, gdy pojazd jest gotowy (UC-CRM-04)
    private OrderState state;
    private Long version;                  // znacznik wersji dla blokady optymistycznej (infrastruktura)

    public Order(OrderId id, OfferId sourceOfferId, Money requiredDeposit) {
        this(id, sourceOfferId, null, requiredDeposit);
    }

    public Order(OrderId id, OfferId sourceOfferId, SpecificationId specificationId, Money requiredDeposit) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (sourceOfferId == null) {
            throw new IllegalArgumentException("sourceOfferId must not be null.");
        }
        this.id = id;
        this.sourceOfferId = sourceOfferId;
        this.specificationId = specificationId;
        this.requiredDeposit = requiredDeposit;
        this.paymentMethod = null;
        this.paymentStatus = PaymentStatus.UNPAID;
        this.handoverDate = null;
        this.vehicleId = null;
        this.state = OrderState.DRAFT_CREATED;
        this.version = null;
    }

    /**
     * Wołane przez {@link OrderFactory} zaraz po utworzeniu: formalne złożenie zamówienia.
     * Agregat ogłasza OrderPlacedEvent (m.in. dla Kontekstu Rozliczeń i Inwentarza)
     * i przechodzi w stan DRAFT. Pakietowy zasięg — odtwarzanie z repozytorium
     * NIE rejestruje tego zdarzenia.
     */
    void markPlaced() {
        this.state = OrderState.DRAFT;
        registerEvent(new OrderPlacedEvent(
                UUID.randomUUID(), this.id.value(),
                this.specificationId == null ? null : this.specificationId.value(),
                Instant.now()));
    }

    /**
     * UC-CRM-03, krok 4-5: klient deklaruje formę płatności. Agregat decyduje, które
     * zdarzenie opuści kontekst: BankTransferDeclaredEvent (przelew, z kwotą kontraktu)
     * albo FinancingRequestedEvent (kredyt/leasing — uruchamia UC-FIN-01).
     */
    public void declarePaymentMethod(PaymentMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("method must not be null.");
        }
        if (this.state == OrderState.COMPLETED || this.state == OrderState.CANCELLED) {
            throw new InvalidOrderStateException(
                    "Cannot declare payment method. Order is already " + this.state);
        }
        if (this.paymentMethod != null) {
            throw new InvalidOrderStateException("Payment method has already been declared.");
        }
        this.paymentMethod = method;

        if (method == PaymentMethod.BANK_TRANSFER) {
            registerEvent(new BankTransferDeclaredEvent(
                    UUID.randomUUID(), this.id.value(), this.requiredDeposit, Instant.now()));
        } else {
            registerEvent(new FinancingRequestedEvent(
                    UUID.randomUUID(), this.id, Instant.now()));
        }
    }

    // UC-CRM-03 cz.2 (Rys. 19/20 PDF): zaksięgowana wpłata uruchamia realizację.
    public void activate() {
        if (this.state != OrderState.DRAFT_CREATED && this.state != OrderState.DRAFT) {
            throw new InvalidOrderStateException(
                    "Only a freshly placed order can be activated, was: " + this.state);
        }
        this.state = OrderState.IN_PROGRESS;
        registerEvent(new OrderActivatedEvent(
                UUID.randomUUID(), this.id.value(), Instant.now()));
    }

    /** Aktualizacja statusu opłacenia (sygnał z Kontekstu Rozliczeń). */
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        if (paymentStatus == null) {
            throw new IllegalArgumentException("paymentStatus must not be null.");
        }
        this.paymentStatus = paymentStatus;
    }

    /** Przypisanie fizycznego pojazdu (VIN) zgłoszonego przez Inwentarz. */
    public void assignVehicle(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    /**
     * UC-CRM-04, krok 1-2: sygnał z placu (VehicleReadyForHandoverEvent) — pojazd gotowy
     * fizycznie i finansowo. Zamówienie przechodzi w stan "Gotowe do odbioru" i ogłasza
     * zdarzenie, które wyzwala powiadomienie Handlowca.
     */
    public void markAsReadyForHandover() {
        if (this.state == OrderState.COMPLETED || this.state == OrderState.CANCELLED) {
            throw new InvalidOrderStateException(
                    "Cannot change state to READY_FOR_HANDOVER. Order is already " + this.state);
        }
        this.state = OrderState.READY_FOR_HANDOVER;
        registerEvent(new OrderReadyForHandoverEvent(
                UUID.randomUUID(), this.id.value(), Instant.now()));
    }

    /**
     * UC-CRM-04, krok 4-5: Handlowiec ustala z klientem termin odbioru — zamówienie przechodzi
     * w stan "Umówiony na odbiór". Obsługuje także A1 (odroczony odbiór: dalsza data).
     */
    public void scheduleHandover(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date must not be null.");
        }
        if (this.state != OrderState.READY_FOR_HANDOVER) {
            throw new IllegalStateException(
                    "Order must be in READY_FOR_HANDOVER state to schedule handover");
        }
        this.handoverDate = date;
        this.state = OrderState.HANDOVER_SCHEDULED;
    }

    /**
     * UC-CRM-05: rejestracja fizycznego wydania pojazdu (podpisany protokół wydania).
     * Dozwolone z "Umówiony na odbiór" oraz bezpośrednio z "Gotowe do odbioru" (klient
     * odbiera auto na miejscu). Agregat ogłasza zamknięcie zamówienia (OrderCompletedEvent)
     * oraz fakt wydania pojazdu (VehicleHandedOverEvent — m.in. dla Rozliczeń i obsługi
     * posprzedażowej); komendę ReleaseVehicle do Inwentarza wysyła warstwa aplikacji.
     */
    public void confirmHandover() {
        if (this.state != OrderState.HANDOVER_SCHEDULED
                && this.state != OrderState.READY_FOR_HANDOVER) {
            throw new InvalidOrderStateException(
                    "Only an order ready or scheduled for handover can be completed (UC-CRM-05),"
                            + " was: " + this.state);
        }
        this.state = OrderState.COMPLETED;
        registerEvent(new OrderCompletedEvent(
                UUID.randomUUID(), this.id.value(), Instant.now()));
        registerEvent(new VehicleHandedOverEvent(
                UUID.randomUUID(), this.id.value(), Instant.now()));
    }

    /**
     * UC-CRM-05, scenariusz A1: Inwentarz odmówił zwolnienia pojazdu
     * (VehicleInventoryReleasedError). Mechanizm kompensacyjny (saga): cofamy zamówienie do
     * "Gotowe do odbioru" i czyścimy ustaloną datę wydania, aby Handlowiec mógł ponowić
     * odbiór po usunięciu blokady magazynowej.
     */
    public void revertToReadyForHandover() {
        if (this.state != OrderState.COMPLETED && this.state != OrderState.HANDOVER_SCHEDULED) {
            throw new InvalidOrderStateException(
                    "Only a completed or scheduled handover can be reverted to ready-for-handover.");
        }
        this.state = OrderState.READY_FOR_HANDOVER;
        this.handoverDate = null;
    }

    /**
     * Anulowanie zamówienia (rezygnacja klienta). Wydanego pojazdu nie można już anulować.
     * Agregat ogłasza OrderCancelledEvent z powodem — m.in. dla Kontekstu Rozliczeń.
     */
    public void cancelOrder(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank.");
        }
        if (this.state == OrderState.COMPLETED) {
            throw new InvalidOrderStateException(
                    "Cannot cancel order after the vehicle has been handed over.");
        }
        if (this.state == OrderState.CANCELLED) {
            throw new InvalidOrderStateException("Order is already CANCELLED.");
        }
        this.state = OrderState.CANCELLED;
        registerEvent(new OrderCancelledEvent(
                UUID.randomUUID(), this.id.value(), reason, Instant.now()));
    }

    public OrderId getId() {
        return this.id;
    }

    /** Identyfikator oferty źródłowej (audytowalność: zamówienie oparte o zatwierdzone warunki). */
    public OfferId getOfferId() {
        return this.sourceOfferId;
    }

    /** Specyfikacja pojazdu z oferty źródłowej (może być null dla zamówień legacy). */
    public SpecificationId getSpecificationId() {
        return this.specificationId;
    }

    public Money getRequiredDeposit() {
        return this.requiredDeposit;
    }

    public PaymentMethod getPaymentMethod() {
        return this.paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return this.paymentStatus;
    }

    public LocalDate getHandoverDate() {
        return this.handoverDate;
    }

    public String getVehicleId() {
        return this.vehicleId;
    }

    public OrderState getState() {
        return this.state;
    }
}
