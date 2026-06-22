package salon.sales.application.domain.model.order;

import salon.common.model.OrderId;
import salon.sales.application.domain.exception.IllegalOrderStateException;
import salon.sales.application.domain.model.offer.OfferId;

import java.time.LocalDate;

/**
 * AGGREGATE ROOT (Figure 23) — the Order.
 *
 * Created from an accepted offer; references the source offer by id (a disjoint reference).
 * Drives the fulfilment lifecycle: payment method declaration (UC-CRM-03), readiness (UC-CRM-04),
 * handover scheduling and completion (UC-CRM-05). Money and customer data are NOT duplicated here —
 * they are obtained from the source offer when needed.
 */
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

    /** UC-CRM-03: records the payment method declared by the customer. */
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

    /** UC-CRM-04: the vehicle is ready for handover. */
    public void markAsReady() {
        if (this.state != OrderState.IN_PROGRESS) {
            throw new IllegalOrderStateException(
                    "Only an IN_PROGRESS order can become READY_FOR_HANDOVER (current: " + this.state + ").");
        }
        this.state = OrderState.READY_FOR_HANDOVER;
    }

    /** UC-CRM-04: the agreed pickup date is set. */
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

    /** UC-CRM-05: the vehicle is physically handed over and the transaction is completed. */
    public void confirmHandover() {
        if (this.state != OrderState.HANDOVER_SCHEDULED) {
            throw new IllegalOrderStateException(
                    "Only a HANDOVER_SCHEDULED order can be completed (current: " + this.state + ").");
        }
        this.state = OrderState.COMPLETED;
    }

    /** UC-CRM-05 / A1: inventory rejected the release — compensate back to READY_FOR_HANDOVER. */
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
