package salon.billing.application.domain.model.settlement;

import java.util.UUID;

/**
 * Value object (Class diagram — Settlement): global identity of the Settlement aggregate.
 * Immutable, compared by value (record). Created by {@link SettlementFactory}.
 */
public record SettlementId(String value) {

    public SettlementId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SettlementId must not be blank.");
        }
    }

    public static SettlementId generate() {
        return new SettlementId("STL-" + UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value;
    }
}
