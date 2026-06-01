package salon.billing.domain.model.document;

import java.util.UUID;

// Value Object: tożsamość/numer dokumentu. Teraz String (test: new DocumentId("DOC-100")).
public record DocumentId(String value) {

    public DocumentId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("DocumentId must not be blank.");
        }
    }

    public static DocumentId generate() {
        return new DocumentId("DOC-" + UUID.randomUUID());
    }
}
