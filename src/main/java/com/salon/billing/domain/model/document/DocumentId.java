package main.java.com.salon.billing.domain.model.document;

import java.util.UUID;

public record DocumentId(UUID value) {

    public DocumentId {
        if (value == null) {
            throw new IllegalArgumentException("DocumentId must not be null.");
        }
    }

    public static DocumentId generate() {
        return new DocumentId(UUID.randomUUID());
    }
}
