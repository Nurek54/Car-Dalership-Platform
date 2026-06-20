package salon.catalog.application.domain.model.shared;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * Obiekt wartości – kwota pieniężna (cena katalogowa / cena całkowita specyfikacji).
 *
 * Niezmienny, porównywalny przez wartość, operacje bez skutków ubocznych
 * (zwracają nowy egzemplarz zamiast modyfikować bieżący – „jest wymienny”).
 */
public final class Money {

    private final BigDecimal amount;
    private final Currency currency;

    private Money(BigDecimal amount, Currency currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Kwota nie może być null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Waluta nie może być null");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Kwota nie może być ujemna: " + amount);
        }
        this.amount = amount.stripTrailingZeros();
        this.currency = currency;
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode));
    }

    public static Money zero(String currencyCode) {
        return new Money(BigDecimal.ZERO, Currency.getInstance(currencyCode));
    }

    /** Operacja-zapytanie bez skutków ubocznych – zwraca nowy obiekt wartości. */
    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    private void requireSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Niezgodne waluty: " + this.currency + " vs " + other.currency);
        }
    }

    public BigDecimal amount() {
        return amount;
    }

    public Currency currency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount.compareTo(money.amount) == 0 && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + currency.getCurrencyCode();
    }
}
