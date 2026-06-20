package salon.billing.application.domain.model.settlement;

import java.util.UUID;

/**
 * Obiekt wartości (Diagram klas — Settlement): globalna tożsamość agregatu Rozliczenia.
 * Niemutowalny, porównywalny po wartości (rekord). Tworzony przez {@link SettlementFactory}.
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
