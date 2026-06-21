package salon.billing.application.domain.model.document;

import java.util.UUID;

/**
 * Value object (Class diagram — AccountingDocument): identity of the Accounting Document aggregate.
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
