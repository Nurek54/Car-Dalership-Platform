package salon.catalog.application.domain.model.specification;

import java.util.Objects;
import java.util.UUID;

/**
 * Obiekt wartości – globalnie unikatowa tożsamość agregatu VehicleSpecification.
 */
public final class SpecificationId {

    private final UUID value;

    private SpecificationId(UUID value) {
        this.value = Objects.requireNonNull(value, "SpecificationId nie może być null");
    }

    public static SpecificationId generate() {
        return new SpecificationId(UUID.randomUUID());
    }

    public static SpecificationId of(UUID value) {
        return new SpecificationId(value);
    }

    public static SpecificationId of(String value) {
        return new SpecificationId(UUID.fromString(value));
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpecificationId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
