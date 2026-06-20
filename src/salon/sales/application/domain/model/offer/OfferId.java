package salon.sales.application.domain.model.offer;

import java.util.UUID;

public record OfferId(String value) {

    public OfferId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OfferId must not be blank.");
        }
    }

    public static OfferId generate() {
        return new OfferId("OFF-" + UUID.randomUUID());
    }
}
