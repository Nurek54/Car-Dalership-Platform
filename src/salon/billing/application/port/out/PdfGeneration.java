package salon.billing.application.port.out;

import salon.billing.application.domain.model.document.AccountingDocument;

/**
 * OUTBOUND PORT (Fig. 48 — PdfGeneration) — generation of the document's PDF file.
 * Implemented by an infrastructure adapter (e.g. PdfGenerator); the port contains no business logic.
 */
public interface PdfGeneration {

    /** @return content of the generated PDF file. */
    byte[] generatePdf(AccountingDocument document);
}
