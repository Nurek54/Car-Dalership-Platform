package salon.billing.application.domain.model.document;

import java.util.UUID;

/**
 * Obiekt wartości (Diagram klas — AccountingDocument): tożsamość agregatu Dokumentu Księgowego.
 */
public record DocumentId(String value) {

    public DocumentId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("DocumentId must not be blank.");
        }
    }

    public static DocumentId generate() {
        return new DocumentId("DOC-" + UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value;
    }
}
