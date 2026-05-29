package main.java.com.salon.billing.application.port.in;

import main.java.com.salon.billing.domain.model.document.DocumentType;

import java.math.BigDecimal;
import java.util.List;

// Komenda dla UC-ROZ-02.
public record IssueDocumentCommand(DocumentType type,
                                   String buyerName,
                                   String nip,
                                   List<LineData> lines) {

    // Pojedyncza pozycja dokumentu jako surowe dane.
    public record LineData(String description,
                           BigDecimal costAmount,
                           String costCurrency) {
    }
}