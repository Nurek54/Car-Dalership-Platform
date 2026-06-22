package salon.common.model;

import java.util.UUID;

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
