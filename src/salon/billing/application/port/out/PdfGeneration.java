package salon.billing.application.port.out;

import salon.billing.application.domain.model.document.AccountingDocument;

/**
 * Port wyjściowy: generowanie pliku PDF dokumentu.
 * Konkretny adapter (np. biblioteka renderująca PDF) wstrzykiwany w pierścieniu infrastruktury.
 */
public interface PdfGeneration {
    byte[] generatePdf(AccountingDocument document);
}
