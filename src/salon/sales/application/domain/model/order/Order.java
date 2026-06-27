package salon.sales.application.domain.model.order;

import salon.common.event.AbstractAggregateRoot;
import salon.common.model.Money;
import salon.common.model.OrderId;
import salon.sales.application.domain.event.BankTransferDeclaredEvent;
import salon.sales.application.domain.event.FinancingRequestedEvent;
import salon.sales.application.domain.event.OrderActivatedEvent;
import salon.sales.application.domain.event.OrderCancelledEvent;
import salon.sales.application.domain.event.OrderCompletedEvent;
import salon.sales.application.domain.event.OrderReadyForHandoverEvent;
import salon.sales.application.domain.model.offer.OfferId;

import java.time.LocalDate;

public class Order extends AbstractAggregateRoot {

    private final OrderId id;
    private final OfferId sourceOfferId;
    private final Money requiredDeposit;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDate handoverDate;
    private OrderState state;
    private Long version; 

    public Order(OrderId id, OfferId sourceOfferId, Money requiredDeposit) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (sourceOfferId == null) {
            throw new IllegalArgumentException("sourceOfferId must not be null.");
        }
        this.id = id;
        this.sourceOfferId = sourceOfferId;
        this.requiredDeposit = requiredDeposit;
        this.paymentStatus = PaymentStatus.PENDING;
        this.state = OrderState.DRAFT_CREATED;
    }

    
    public Order(OrderId id, OfferId sourceOfferId) {
        this(id, sourceOfferId, null);
    }

    
    public void activate() {
        if (this.state != OrderState.DRAFT_CREATED) {
            throw new InvalidOrderStateException(
                    "Only a DRAFT_CREATED order can be activated (current: " + this.state + ").");
        }
        this.state = OrderState.IN_PROGRESS;
        registerEvent(new OrderActivatedEvent(this.id.value()));
    }

    
    public void declarePaymentMethod(PaymentMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("method must not be null.");
        }
        if (this.state != OrderState.DRAFT_CREATED && this.state != OrderState.IN_PROGRESS) {
            throw new InvalidOrderStateException(
                    "Payment method can be declared only on a DRAFT_CREATED or IN_PROGRESS order (current: "
                            + this.state + ").");
        }
        this.paymentMethod = method;
        if (method == PaymentMethod.BANK_TRANSFER) {
            registerEvent(new BankTransferDeclaredEvent(this.id.value(), this.requiredDeposit));
        } else {
            registerEvent(new FinancingRequestedEvent(this.id.value(), null));
        }
    }

    
    public void changePaymentStatus(PaymentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null.");
        }
        this.paymentStatus = status;
    }

    
    public void markAsReadyForHandover() {
        if (this.state == OrderState.COMPLETED) {
            throw new InvalidOrderStateException(
                    "Cannot change state to READY_FOR_HANDOVER. Order is already COMPLETED");
        }
        if (this.state != OrderState.IN_PROGRESS) {
            throw new InvalidOrderStateException(
                    "Only an IN_PROGRESS order can become READY_FOR_HANDOVER (current: " + this.state + ").");
        }
        this.state = OrderState.READY_FOR_HANDOVER;
        registerEvent(new OrderReadyForHandoverEvent(this.id.value()));
    }

    
    public void markAsReady() {
        markAsReadyForHandover();
    }

    
    public void scheduleHandover(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date must not be null.");
        }
        if (this.state != OrderState.READY_FOR_HANDOVER) {
            throw new InvalidOrderStateException(
                    "Order must be in READY_FOR_HANDOVER state to schedule handover (current: " + this.state + ").");
        }
        this.handoverDate = date;
        this.state = OrderState.HANDOVER_SCHEDULED;
    }

    
    public void confirmHandover() {
        if (this.state != OrderState.READY_FOR_HANDOVER && this.state != OrderState.HANDOVER_SCHEDULED) {
            throw new InvalidOrderStateException(
                    "Only a READY_FOR_HANDOVER or HANDOVER_SCHEDULED order can be completed (current: "
                            + this.state + ").");
        }
        this.state = OrderState.COMPLETED;
        registerEvent(new OrderCompletedEvent(this.id.value()));
    }

    
    public void revertToReadyForHandover() {
        if (this.state != OrderState.COMPLETED && this.state != OrderState.HANDOVER_SCHEDULED) {
            throw new InvalidOrderStateException(
                    "Only a COMPLETED or HANDOVER_SCHEDULED order can be reverted (current: " + this.state + ").");
        }
        this.handoverDate = null;
        this.state = OrderState.READY_FOR_HANDOVER;
    }

    
    public void cancel(String reason) {
        if (this.state == OrderState.COMPLETED || this.state == OrderState.CANCELLED) {
            throw new InvalidOrderStateException(
                    "A " + this.state + " order cannot be cancelled.");
        }
        this.state = OrderState.CANCELLED;
        registerEvent(new OrderCancelledEvent(this.id.value(), reason));
    }

    

    public OrderId id() {
        return id;
    }

    public OfferId offerId() {
        return sourceOfferId;
    }

    public OfferId sourceOfferId() {
        return sourceOfferId;
    }

    public Money requiredDeposit() {
        return requiredDeposit;
    }

    public PaymentMethod paymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus paymentStatus() {
        return paymentStatus;
    }

    public LocalDate handoverDate() {
        return handoverDate;
    }

    public OrderState state() {
        return state;
    }
    

    public Long version() {
        return version;
    }

    
    public static Order reconstitute(OrderId id, OfferId sourceOfferId, Money requiredDeposit,
                                     PaymentMethod paymentMethod, PaymentStatus paymentStatus,
                                     LocalDate handoverDate, OrderState state, Long version) {
        Order order = new Order(id, sourceOfferId, requiredDeposit);
        order.paymentMethod = paymentMethod;
        order.paymentStatus = paymentStatus;
        order.handoverDate = handoverDate;
        order.state = state;
        order.version = version;
        return order;
    }
}
