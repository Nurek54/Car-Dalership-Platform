package main.java.com.salon.billing.domain.model.payment;

import main.java.com.salon.billing.domain.model.shared.Money;
import main.java.com.salon.billing.domain.model.shared.OrderId;

/**
 * Aggregate Root: pojedyncza wpłata klienta.
 *
 * Hermetyzacja: pola prywatne, brak setterów. Kategorię można ustawić
 * WYŁĄCZNIE przez metodę biznesową categorizePayment().
 */
public class Payment {

    private final PaymentId id;
    private final OrderId orderId;
    private final Money amount;
    private PaymentCategory category; // null, dopóki wpłata nie zostanie sklasyfikowana

    public Payment(PaymentId id, OrderId orderId, Money amount) {
        if (id == null) {
            throw new IllegalArgumentException("id nie może być nullem.");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("orderId nie może być nullem.");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount nie może być nullem.");
        }
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.category = null;
    }

    /**
     * UC-ROZ-01, krok 3: nadanie kategorii biznesowej.
     * Reguła: jeśli wpłata >= wymagany zadatek -> ZADATEK, w przeciwnym razie -> ZALICZKA (A2).
     */
    public void categorizePayment(Money requiredDeposit) {
        if (requiredDeposit == null) {
            throw new IllegalArgumentException("requiredDeposit nie może być nullem.");
        }
        if (this.amount.isGreaterThanOrEqualTo(requiredDeposit)) {
            this.category = PaymentCategory.DEPOSIT;
        } else {
            this.category = PaymentCategory.ADVANCE;
        }
    }

    public boolean isDeposit() {
        return this.category == PaymentCategory.DEPOSIT;
    }

    public PaymentId getId() {
        return this.id;
    }

    public OrderId getOrderId() {
        return this.orderId;
    }

    public Money getAmount() {
        return this.amount;
    }

    public PaymentCategory getCategory() {
        return this.category;
    }
}