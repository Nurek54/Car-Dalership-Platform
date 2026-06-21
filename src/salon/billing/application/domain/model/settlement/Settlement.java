package salon.billing.application.domain.model.settlement;

import salon.billing.application.domain.event.AdvancePaymentRegisteredEvent;
import salon.billing.application.domain.event.AdvancePaymentRequestedEvent;
import salon.billing.application.domain.event.PaymentRegisteredEvent;
import salon.billing.application.domain.event.SettlementCompletedEvent;
import salon.common.event.AbstractAggregateRoot;
import salon.common.model.Money;
import salon.common.model.OrderId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AGGREGATE ROOT (Class diagram — «AggregateRoot» Settlement).
 *
 * Guards the transactional consistency of the order balance: the list of payments ({@link Payment}) and the status
 * ({@link SettlementStatus}) change only through the root's commands, atomically in a single
 * transaction (UC-FIR-03). The reference to the order is disjoint — only through {@link OrderId}
 * (Shared Kernel). It records events using the „collect &amp; pull" pattern ({@link AbstractAggregateRoot}):
 * the decision "which event" stays in the domain, and the application service pulls and publishes it.
 */
public class Settlement extends AbstractAggregateRoot {

    private final SettlementId id;
    private final OrderId orderId;
    private final Money totalAmount;
    private final List<Payment> payments;
    private SettlementStatus status;

    /** Whether a deposit request was sent (UC-FIR-01) — controls the emission of AdvancePaymentRegisteredEvent. */
    private boolean advanceRequested;

    /** Package-private constructor — instances are created only by {@link SettlementFactory}. */
    Settlement(SettlementId id, OrderId orderId, Money totalAmount, SettlementStatus status) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        if (totalAmount == null) {
            throw new IllegalArgumentException("totalAmount must not be null.");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null.");
        }
        this.id = id;
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.payments = new ArrayList<>();
        this.status = status;
        this.advanceRequested = false;
    }

    /**
     * UC-FIR-01, steps 2-4: marking the deposit as due and emitting
     * {@link AdvancePaymentRequestedEvent} (the customer will be asked to pay).
     */
    public void requestAdvancePayment() {
        this.advanceRequested = true;
        registerEvent(new AdvancePaymentRequestedEvent(this.orderId.value()));
    }

    /**
     * UC-FIR-03: posting the transfer and recomputing the balance — an atomic invariant.
     * Records {@link PaymentRegisteredEvent}; if this is the first payment after the deposit request,
     * additionally {@link AdvancePaymentRegisteredEvent} (triggers production — UC-INW-02).
     */
    public void registerPayment(String transactionId, Money amount) {
        if (this.status == SettlementStatus.SETTLED) {
            throw new salon.billing.application.domain.exception.IllegalSettlementStateException(
                    "The balance of order " + this.orderId.value() + " is already settled (SETTLED).");
        }
        boolean firstPayment = this.payments.isEmpty();
        this.payments.add(new Payment(transactionId, amount, LocalDateTime.now()));
        registerEvent(new PaymentRegisteredEvent(this.orderId.value()));
        if (firstPayment && this.advanceRequested) {
            registerEvent(new AdvancePaymentRegisteredEvent(this.orderId.value()));
        }
        recalculateBalance();
    }

    /**
     * Balance adjustment policy (private — root invariant): balance = 0 -> SETTLED
     * (+ {@link SettlementCompletedEvent}); a partial payment (A1) -> PARTIAL_PAYMENT.
     */
    private void recalculateBalance() {
        boolean fullyPaid = outstandingBalance().getAmount().signum() <= 0;
        if (fullyPaid) {
            if (this.status != SettlementStatus.SETTLED) {
                this.status = SettlementStatus.SETTLED;
                registerEvent(new SettlementCompletedEvent(this.orderId.value()));
            }
        } else {
            this.status = SettlementStatus.PARTIAL_PAYMENT;
        }
    }

    /** Remaining balance due = contract amount - sum of posted payments (includes the deposit). */
    public Money outstandingBalance() {
        Money paid = Money.of(BigDecimal.ZERO, this.totalAmount.currency());
        for (Payment payment : this.payments) {
            paid = paid.add(payment.amount());
        }
        return this.totalAmount.subtract(paid);
    }

    public SettlementId id() {
        return id;
    }

    public OrderId orderId() {
        return orderId;
    }

    public Money totalAmount() {
        return totalAmount;
    }

    public SettlementStatus status() {
        return status;
    }

    public boolean isAdvanceRequested() {
        return advanceRequested;
    }

    public List<Payment> payments() {
        return Collections.unmodifiableList(new ArrayList<>(this.payments));
    }
}
