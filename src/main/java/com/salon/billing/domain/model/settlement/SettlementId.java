package main.java.com.salon.billing.domain.model.settlement;

import java.util.UUID;

public record SettlementId(UUID value) {

    public SettlementId {
        if (value == null) {
            throw new IllegalArgumentException("SettlementId must not be null.");
        }
    }

    public static SettlementId generate() {
        return new SettlementId(UUID.randomUUID());
    }
}
