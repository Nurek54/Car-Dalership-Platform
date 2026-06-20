package salon.catalog.application.domain.model.shared;

import java.util.Objects;

/**
 * Obiekt wartości (Value Object) – kod opcji wyposażenia (pakiet, silnik, skrzynia, kolor).
 *
 * Cechy obiektu wartości (wg PDF, za Vernonem):
 *  - nie ma tożsamości,
 *  - jest niezmienny (stan ustawia tylko konstruktor),
 *  - jest porównywalny przez wartość wszystkich atrybutów (equals/hashCode),
 *  - jego operacje są pozbawione skutków ubocznych.
 */
public final class OptionCode {

    private final String value;

    private OptionCode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OptionCode nie może być pusty");
        }
        this.value = value.trim().toUpperCase();
    }

    /** Metoda wytwórcza zgodna z językiem wszechobecnym. */
    public static OptionCode of(String value) {
        return new OptionCode(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OptionCode that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
