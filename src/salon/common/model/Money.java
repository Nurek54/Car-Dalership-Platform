package salon.common.model;

import java.math.BigDecimal;

/**
 * Value Object: a monetary amount (value + currency) — the shared kernel (Shared Kernel).
 *
 * Money is immutable: operations (add, subtract) return a NEW object.
 * The record gives us equals()/hashCode() "by value" for free.
 */
public record Money(BigDecimal amount, String currency) {

    // Compact constructor — enforces the invariants on every object creation.
    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Amount must not be null.");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency is required.");
        }
        // Note: we do NOT block negative values — the final balance can be negative (overpayment).
    }

    // Factory used in tests: Money.of(new BigDecimal("5000.00"), "PLN").
    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    // Convenience factory for integers: Money.of(2000, "PLN").
    public static Money of(long amount, String currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    // "JavaBean"-style getter — tests call getAmount(); the record also has amount().
    public BigDecimal getAmount() {
        return this.amount;
    }

    public Money add(Money other) {
        checkSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        checkSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public boolean isGreaterThanOrEqualTo(Money other) {
        checkSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0;
    }

    public boolean isLessThan(Money other) {
        checkSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isNegative() {
        return this.amount.signum() < 0;
    }

    // A simple guard: currencies must not be mixed.
    private void checkSameCurrency(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("The other amount must not be null.");
        }
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Currency mismatch: " + this.currency + " and " + other.currency);
        }
    }
}
