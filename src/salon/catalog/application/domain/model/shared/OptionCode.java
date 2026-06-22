package salon.catalog.application.domain.model.shared;

import java.util.Objects;

public final class OptionCode {

    private final String value;

    private OptionCode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OptionCode cannot be empty");
        }
        this.value = value.trim().toUpperCase();
    }

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
