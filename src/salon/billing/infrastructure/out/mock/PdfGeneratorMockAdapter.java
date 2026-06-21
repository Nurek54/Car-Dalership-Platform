package salon.billing.infrastructure.out.mock;

import salon.billing.application.domain.model.document.AccountingDocument;
import salon.billing.application.port.out.PdfGeneration;

/**
 * OUTBOUND ADAPTER (Fig. 48 — PdfGenerator) — a mock of the {@link PdfGeneration} port.
 *
 * Delegates to the PDF representation of the aggregate root ({@link AccountingDocument#generatePdf()});
 * a real adapter would wrap a PDF-rendering library here, without business logic.
 */
public class PdfGeneratorMockAdapter implements PdfGeneration {

    @Override
    public byte[] generatePdf(AccountingDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("document must not be null.");
        }
        byte[] pdf = document.generatePdf();
        System.out.println("[PdfGeneratorMockAdapter] Generated PDF for document "
                + document.id().value() + " (" + pdf.length + " B).");
        return pdf;
    }
}
