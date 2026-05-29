package main.java.com.salon.billing.domain.model.document;

import main.java.com.salon.billing.domain.model.shared.Money;

/**
 * Encja LOKALNA wewnątrz agregatu AccountingDocument.
 * Nie jest osobnym Aggregate Rootem: nie ma własnego repozytorium,
 * tworzy się i zapisuje razem z dokumentem.
 */
public class DocumentLine {

    private final LineId id;
    private final String description;
    private final Money cost;

    public DocumentLine(LineId id, String description, Money cost) {
        if (id == null) {
            throw new IllegalArgumentException("id pozycji nie może być nullem.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Opis pozycji (description) jest wymagany.");
        }
        if (cost == null) {
            throw new IllegalArgumentException("Koszt pozycji (cost) nie może być nullem.");
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