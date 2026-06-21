package salon.catalog.application.domain.model.shared;

import java.util.Objects;

/**
 * Value Object – an equipment option code (package, engine, gearbox, color).
 *
 * Characteristics of a value object (per the PDF, after Vernon):
 *  - it has no identity,
 *  - it is immutable (state is set only by the constructor),
 *  - it is compared by the value of all attributes (equals/hashCode),
 *  - its operations are free of side effects.
 */
public final class OptionCode {

    private final String value;

    private OptionCode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OptionCode cannot be empty");
        }
        this.value = value.trim().toUpperCase();
    }

    /** Factory method consistent with the ubiquitous language. */
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
