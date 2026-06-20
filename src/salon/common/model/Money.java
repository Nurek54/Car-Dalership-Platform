package salon.common.model;

import java.math.BigDecimal;

/**
 * Value Object: kwota pieniężna (wartość + waluta) — wspólny rdzeń (Shared Kernel).
 *
 * Money jest niemutowalne: operacje (add, subtract) zwracają NOWY obiekt.
 * Rekord daje nam equals()/hashCode() "po wartości" za darmo.
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

    // Fabryka używana w testach: Money.of(new BigDecimal("5000.00"), "PLN").
    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    // Wygodna fabryka dla liczb całkowitych: Money.of(2000, "PLN").
    public static Money of(long amount, String currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    // Getter w stylu "JavaBean" — testy wołają getAmount(); rekord ma też amount().
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
