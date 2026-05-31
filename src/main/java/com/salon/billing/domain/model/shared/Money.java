package main.java.com.salon.billing.domain.model.shared;

import java.math.BigDecimal;

/**
 * Value Object: kwota pieniężna (wartość + waluta).
 *
 * Dlaczego record? Bo VO porównujemy "po wartości" (dwie kwoty 100 PLN są równe),
 * a record robi equals()/hashCode() za nas. To nie magia — to wbudowana cecha rekordów.
 *
 * Money jest niemutowalne: operacje (add, subtract) zwracają NOWY obiekt.
 */
public record Money(BigDecimal amount, String currency) {

    // Konstruktor kompaktowy — pilnuje niezmienników przy każdym tworzeniu obiektu.
    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Amount must not be null.");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency is required.");
        }
        // Uwaga: NIE blokujemy wartości ujemnych — saldo końcowe może być ujemne (nadpłata).
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

    // Prosta straż: nie wolno mieszać walut.
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
