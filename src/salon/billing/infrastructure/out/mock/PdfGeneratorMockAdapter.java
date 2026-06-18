package salon.billing.infrastructure.out.mock;

import salon.billing.application.port.out.PdfGeneration;
import salon.billing.application.domain.model.document.AccountingDocument;

import java.nio.charset.StandardCharsets;

// Udawany generator PDF: generuje reprezentację bajtową bezpośrednio w adapterze i loguje rozmiar.
public class PdfGeneratorMockAdapter implements PdfGeneration {

    @Override
    public byte[] generatePdf(AccountingDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }

        // Logika generowania tymczasowej zawartości tekstowej przeniesiona z agregatu
        String content = "INVOICE " + document.getId().value()
                + " | title=" + document.getInvoiceTitle()
                + " | buyer=" + document.getBuyer().name()
                + " | seller=" + document.getSeller().name()
                + " | amount=" + document.getTotalAmount().amount() + " " + document.getTotalAmount().currency()
                + " | issued=" + document.getIssueDate() + " | due=" + document.getDueDate()
                + " | issuer=" + document.getAuthorizedIssuer();

        byte[] pdf = content.getBytes(StandardCharsets.UTF_8);

        System.out.println("[PdfGeneratorMock] Generated PDF for " + document.getId().value()
                + " (" + pdf.length + " bytes).");
        return pdf;
    }
}