package salon.billing.infrastructure.out.mock;

import salon.billing.application.domain.model.document.AccountingDocument;
import salon.billing.application.port.out.PdfGeneration;

/**
 * ADAPTER WYJSCIOWY (Rys. 48 — PdfGenerator) — atrapa portu {@link PdfGeneration}.
 *
 * Deleguje do reprezentacji PDF korzenia agregatu ({@link AccountingDocument#generatePdf()});
 * realny adapter opakowalby tu bibliotekę renderujaca PDF, bez logiki biznesowej.
 */
public class PdfGeneratorMockAdapter implements PdfGeneration {

    @Override
    public byte[] generatePdf(AccountingDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("document must not be null.");
        }
        byte[] pdf = document.generatePdf();
        System.out.println("[PdfGeneratorMockAdapter] Wygenerowano PDF dla dokumentu "
                + document.getId().value() + " (" + pdf.length + " B).");
        return pdf;
    }
}
