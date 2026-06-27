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

public class Settlement extends AbstractAggregateRoot {

    private final SettlementId id;
    private final OrderId orderId;
    private final Money totalAmount;
    private final List<Payment> payments;
    private SettlementStatus status;

    
    private boolean advanceRequested;

    
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

    
    public void requestAdvancePayment() {
        this.advanceRequested = true;
        registerEvent(new AdvancePaymentRequestedEvent(this.orderId.value()));
    }

    
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

    
    private void recalculateBalance() {
        boolean fullyPaid = outstandingBalance().amount().signum() <= 0;
        if (fullyPaid) {
            if (this.status != SettlementStatus.SETTLED) {
                this.status = SettlementStatus.SETTLED;
                registerEvent(new SettlementCompletedEvent(this.orderId.value()));
            }
        } else {
            this.status = SettlementStatus.PARTIAL_PAYMENT;
        }
    }

    
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
