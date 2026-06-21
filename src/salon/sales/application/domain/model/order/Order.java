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
 * Aggregate Root: the order (UC-CRM-03, UC-CRM-04, UC-CRM-05).
 *
 * Model consistent with docs/Agregate/Sales/customer-offer-order.md and docs/Agregate/Sales/order.md:
 *   pola:    id, sourceOfferId, requiredDeposit, paymentMethod, paymentStatus,
 *            handoverDate, state (+ vehicleId assigned upon readiness for handover)
 *   metody:  declarePaymentMethod, activate, markAsReadyForHandover, scheduleHandover,
 *            confirmHandover, revertToReadyForHandover, cancelOrder
 *   states:  DRAFT_CREATED/DRAFT -> IN_PROGRESS -> READY_FOR_HANDOVER
 *            -> HANDOVER_SCHEDULED -> COMPLETED (CANCELLED — customer cancellation)
 *
 * Payment policy: declarePaymentMethod() encapsulates the decision about which event
 * will leave the aggregate (BankTransferDeclaredEvent / FinancingRequestedEvent).
 * Compensating mechanism (saga): revertToReadyForHandover() allows safely reverting
 * the order after Inventory refuses to remove the vehicle from stock (UC-CRM-05, A1) —
 * clearing the agreed handover date.
 */
public class Order extends AbstractAggregateRoot {

    private final OrderId id;
    private final OfferId sourceOfferId;
    private final SpecificationId specificationId; // specification from the offer (event-carried state transfer to Inventory)
    private final Money requiredDeposit;   // the negotiated contract value (may be null)

    private PaymentMethod paymentMethod;   // the declared payment method (UC-CRM-03, step 4)
    private PaymentStatus paymentStatus;   // synchronized from the Billing events
    private LocalDate handoverDate;        // the agreed pickup date (UC-CRM-04)
    private String vehicleId;              // VIN assigned when the vehicle is ready (UC-CRM-04)
    private OrderState state;
    private Long version;                  // version marker for optimistic locking (infrastructure)

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
     * Called by {@link OrderFactory} right after creation: the formal placement of the order.
     * The aggregate announces OrderPlacedEvent (among others for the Billing and Inventory Contexts)
     * and transitions to the DRAFT state. Package scope — reconstitution from the repository
     * does NOT record this event.
     */
    void markPlaced() {
        this.state = OrderState.DRAFT;
        registerEvent(new OrderPlacedEvent(
                UUID.randomUUID(), this.id.value(),
                this.specificationId == null ? null : this.specificationId.value(),
                Instant.now()));
    }

    /**
     * UC-CRM-03, steps 4-5: the customer declares the payment method. The aggregate decides which
     * event will leave the context: BankTransferDeclaredEvent (transfer, with the contract amount)
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

    // UC-CRM-03 part 2 (Fig. 19/20 PDF): a posted payment triggers fulfillment.
    public void activate() {
        if (this.state != OrderState.DRAFT_CREATED && this.state != OrderState.DRAFT) {
            throw new InvalidOrderStateException(
                    "Only a freshly placed order can be activated, was: " + this.state);
        }
        this.state = OrderState.IN_PROGRESS;
        registerEvent(new OrderActivatedEvent(
                UUID.randomUUID(), this.id.value(), Instant.now()));
    }

    /** Updating the payment status (signal from the Billing Context). */
    public void changePaymentStatus(PaymentStatus paymentStatus) {
        if (paymentStatus == null) {
            throw new IllegalArgumentException("paymentStatus must not be null.");
        }
        this.paymentStatus = paymentStatus;
    }

    /** Assigning the physical vehicle (VIN) reported by Inventory. */
    public void assignVehicle(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    /**
     * UC-CRM-04, steps 1-2: a signal from the yard (VehicleReadyForHandoverEvent) — the vehicle is ready
     * physically and financially. The order transitions to the "Ready for handover" state and announces
     * an event that triggers the notification of the Salesperson.
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
     * UC-CRM-04, steps 4-5: the Salesperson agrees the pickup date with the customer — the order transitions
     * to the "Handover scheduled" state. It also supports A1 (deferred pickup: a later date).
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
     * UC-CRM-05: registering the physical handover of the vehicle (a signed handover protocol).
     * Allowed from "Handover scheduled" and directly from "Ready for handover" (the customer
     * picks up the car on the spot). The aggregate announces the order closure (OrderCompletedEvent)
     * and the fact of the vehicle handover (VehicleHandedOverEvent — among others for Billing and after-sales
     * support); the ReleaseVehicle command to Inventory is sent by the application layer.
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
     * UC-CRM-05, scenario A1: Inventory refused to release the vehicle
     * (VehicleInventoryReleasedError). Compensating mechanism (saga): we revert the order to
     * "Ready for handover" and clear the agreed handover date, so the Salesperson can retry
     * the handover after the stock lock is removed.
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
     * Cancelling the order (customer cancellation). A handed-over vehicle can no longer be cancelled.
     * The aggregate announces OrderCancelledEvent with a reason — among others for the Billing Context.
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

    public OrderId id() {
        return this.id;
    }

    /** Identifier of the source offer (auditability: the order is based on approved terms). */
    public OfferId offerId() {
        return this.sourceOfferId;
    }

    /** Vehicle specification from the source offer (may be null for legacy orders). */
    public SpecificationId specificationId() {
        return this.specificationId;
    }

    public Money requiredDeposit() {
        return this.requiredDeposit;
    }

    public PaymentMethod paymentMethod() {
        return this.paymentMethod;
    }

    public PaymentStatus paymentStatus() {
        return this.paymentStatus;
    }

    public LocalDate handoverDate() {
        return this.handoverDate;
    }

    public String vehicleId() {
        return this.vehicleId;
    }

    public OrderState state() {
        return this.state;
    }
}
