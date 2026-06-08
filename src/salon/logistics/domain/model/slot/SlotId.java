package salon.logistics.domain.model.slot;

import java.util.UUID;

/**
 * Value Object: identyfikator slotu produkcyjnego (ścieżka Long Track).
 */
public record SlotId(String value) {

    public SlotId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SlotId must not be blank.");
        }
    }

    public static SlotId generate() {
        return new SlotId("SLOT-" + UUID.randomUUID());
    }
}
