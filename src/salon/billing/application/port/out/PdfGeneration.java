package salon.billing.application.port.out;

import salon.billing.application.domain.model.document.AccountingDocument;

public interface PdfGeneration {

    byte[] generatePdf(AccountingDocument document);
}
