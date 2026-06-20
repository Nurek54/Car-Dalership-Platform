package salon.billing.application.port.out;

import salon.billing.application.domain.model.document.AccountingDocument;

/**
 * PORT WYJSCIOWY (Rys. 48 — PdfGeneration) — generowanie pliku PDF dokumentu.
 * Implementowany przez adapter infrastruktury (np. PdfGenerator); port nie zawiera logiki biznesowej.
 */
public interface PdfGeneration {

    /** @return zawartosc wygenerowanego pliku PDF. */
    byte[] generatePdf(AccountingDocument document);
}
