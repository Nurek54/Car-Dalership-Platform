package salon.catalog.application.domain.model.catalog;

import java.util.Objects;
import java.util.UUID;

/**
 * Obiekt wartości – globalnie unikatowa tożsamość agregatu ProductCatalog.
 *
 * Wg Reguły 3 projektowania agregatów: VehicleSpecification odwołuje się do
 * ProductCatalog WYŁĄCZNIE przez ten identyfikator (a nie przez referencję),
 * dzięki czemu agregaty pozostają rozłączne.
 */
public final class CatalogId {

    private final UUID value;

    private CatalogId(UUID value) {
        this.value = Objects.requireNonNull(value, "CatalogId nie może być null");
    }

    public static CatalogId generate() {
        return new CatalogId(UUID.randomUUID());
    }

    public static CatalogId of(UUID value) {
        return new CatalogId(value);
    }

    public static CatalogId of(String value) {
        return new CatalogId(UUID.fromString(value));
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CatalogId catalogId)) return false;
        return value.equals(catalogId.value);
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
