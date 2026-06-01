package salon.billing.domain.model.document;

import salon.shared.model.Money;

/**
 * Encja LOKALNA wewnątrz agregatu AccountingDocument.
 * Nie jest osobnym Aggregate Rootem: tworzy się i zapisuje razem z dokumentem.
 */
public class DocumentLine {

    private final LineId id;
    private final String description;
    private final Money cost;

    public DocumentLine(LineId id, String description, Money cost) {
        if (id == null) {
            throw new IllegalArgumentException("Line id must not be null.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Line description (description) is required.");
        }
        if (cost == null) {
            throw new IllegalArgumentException("Line cost (cost) must not be null.");
        }
        this.id = id;
        this.description = description;
        this.cost = cost;
    }

    public LineId getId() {
        return this.id;
    }

    public String getDescription() {
        return this.description;
    }

    public Money getCost() {
        return this.cost;
    }
}
