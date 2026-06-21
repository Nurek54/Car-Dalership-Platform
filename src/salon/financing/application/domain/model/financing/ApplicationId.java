package salon.financing.application.domain.model.financing;

import java.util.UUID;

/**
 * Value object: identity of the Financing Application aggregate (Figure 43).
 */
public record ApplicationId(String value) {

    public ApplicationId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ApplicationId must not be blank.");
        }
    }

    public static ApplicationId generate() {
        return new ApplicationId("FIN-" + UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value;
    }
}
