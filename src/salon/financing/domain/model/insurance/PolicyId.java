package salon.financing.domain.model.insurance;

import java.util.UUID;

public record PolicyId(String value) {

    public PolicyId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PolicyId must not be blank.");
        }
    }

    public static PolicyId generate() {
        return new PolicyId("POL-" + UUID.randomUUID());
    }
}
