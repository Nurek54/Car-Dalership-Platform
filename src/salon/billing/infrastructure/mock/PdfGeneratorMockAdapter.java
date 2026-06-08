package salon.billing.infrastructure.mock;

import salon.billing.application.port.out.PdfGeneratorPort;
import salon.billing.domain.model.document.AccountingDocument;

// Udawany generator PDF: deleguje do reprezentacji bajtowej agregatu i loguje rozmiar.
public class PdfGeneratorMockAdapter implements PdfGeneratorPort {

    @Override
    public byte[] generatePdf(AccountingDocument document) {
        byte[] pdf = document.generatePdf();
        System.out.println("[PdfGeneratorMock] Generated PDF for " + document.getId().value()
                + " (" + pdf.length + " bytes).");
        return pdf;
    }
}
