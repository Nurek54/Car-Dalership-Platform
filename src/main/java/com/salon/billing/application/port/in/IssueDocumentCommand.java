package main.java.com.salon.billing.application.port.in;

import main.java.com.salon.billing.domain.model.document.DocumentType;

import java.math.BigDecimal;
import java.util.List;

// Komenda dla UC-ROZ-02.
public record IssueDocumentCommand(DocumentType type,
                                   String buyerName,
                                   String nip,
                                   List<LineData> lines) {

    // Walidacja danych wejściowych komendy.
    public IssueDocumentCommand {
        if (type == null) {
            throw new IllegalArgumentException("Document type must not be null.");
        }
        if (buyerName == null || buyerName.isBlank()) {
            throw new IllegalArgumentException("Buyer name is required.");
        }
        // A2: dokument bez pozycji nie ma sensu — odrzucamy już na wejściu.
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("At least one document line is required.");
        }
        // nip może być pusty (paragon) — dlatego go nie walidujemy.
    }

    // Pojedyncza pozycja dokumentu jako surowe dane.
    public record LineData(String description,
                           BigDecimal costAmount,
                           String costCurrency) {

        public LineData {
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("Line description is required.");
            }
            if (costAmount == null) {
                throw new IllegalArgumentException("Line cost amount must not be null.");
            }
            if (costCurrency == null || costCurrency.isBlank()) {
                throw new IllegalArgumentException("Line cost currency is required.");
            }
        }
    }
}
