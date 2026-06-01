package salon.billing.domain.model.settlement;

import java.util.UUID;

// Value Object: tożsamość rozliczenia. String (test: new SettlementId("SET-001")).
public record SettlementId(String value) {

    public SettlementId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SettlementId must not be blank.");
        }
    }

    public static SettlementId generate() {
        return new SettlementId("SET-" + UUID.randomUUID());
    }
}
