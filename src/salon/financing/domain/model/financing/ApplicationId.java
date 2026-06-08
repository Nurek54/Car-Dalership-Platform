package salon.financing.domain.model.financing;

import java.util.UUID;

public record ApplicationId(String value) {

    public ApplicationId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ApplicationId must not be blank.");
        }
    }

    public static ApplicationId generate() {
        return new ApplicationId("FIN-" + UUID.randomUUID());
    }
}
