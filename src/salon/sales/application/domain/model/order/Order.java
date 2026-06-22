package salon.sales.application.domain.model.order;

import salon.common.model.OrderId;
import salon.sales.application.domain.exception.IllegalOrderStateException;
import salon.sales.application.domain.model.offer.OfferId;

import java.time.LocalDate;

public class Order {

    private final OrderId id;
    private final OfferId sourceOfferId;
    private PaymentMethod paymentMethod;
    private LocalDate handoverDate;
    private OrderState state;

    public Order(OrderId id, OfferId sourceOfferId) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (sourceOfferId == null) {
            throw new IllegalArgumentException("sourceOfferId must not be null.");
        }
        this.id = id;
        this.sourceOfferId = sourceOfferId;
        this.state = OrderState.IN_PROGRESS;
    }

    public void declarePaymentMethod(PaymentMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("method must not be null.");
        }
        if (this.state != OrderState.IN_PROGRESS) {
            throw new IllegalOrderStateException(
                    "Payment method can be declared only on an IN_PROGRESS order (current: " + this.state + ").");
        }
        this.paymentMethod = method;
    }

    public void markAsReady() {
        if (this.state != OrderState.IN_PROGRESS) {
            throw new IllegalOrderStateException(
                    "Only an IN_PROGRESS order can become READY_FOR_HANDOVER (current: " + this.state + ").");
        }
        this.state = OrderState.READY_FOR_HANDOVER;
    }

    public void scheduleHandover(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date must not be null.");
        }
        if (this.state != OrderState.READY_FOR_HANDOVER) {
            throw new IllegalOrderStateException(
                    "Handover can be scheduled only for a READY_FOR_HANDOVER order (current: " + this.state + ").");
        }
        this.handoverDate = date;
        this.state = OrderState.HANDOVER_SCHEDULED;
    }

    public void confirmHandover() {
        if (this.state != OrderState.HANDOVER_SCHEDULED) {
            throw new IllegalOrderStateException(
                    "Only a HANDOVER_SCHEDULED order can be completed (current: " + this.state + ").");
        }
        this.state = OrderState.COMPLETED;
    }

    public void revertToReadyForHandover() {
        if (this.state != OrderState.COMPLETED && this.state != OrderState.HANDOVER_SCHEDULED) {
            throw new IllegalOrderStateException(
                    "Only a COMPLETED or HANDOVER_SCHEDULED order can be reverted (current: " + this.state + ").");
        }
        this.handoverDate = null;
        this.state = OrderState.READY_FOR_HANDOVER;
    }

    public OrderId getId() {
        return id;
    }

    public OfferId getSourceOfferId() {
        return sourceOfferId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDate getHandoverDate() {
        return handoverDate;
    }

    public OrderState getState() {
        return state;
    }
}
