package salon.common.model;

import java.util.UUID;

/**
 * Value Object: globalny identyfikator Specyfikacji Pojazdu.
 * Wspólny rdzeń: Katalog go "wystawia", Sprzedaż się do niego odwołuje (referencja).
 */
public record SpecificationId(String value) {

    public SpecificationId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SpecificationId must not be blank.");
        }
    }

    public static SpecificationId generate() {
        return new SpecificationId("SPEC-" + UUID.randomUUID());
    }
}
