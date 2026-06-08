package salon.billing.infrastructure.mock;

import salon.billing.application.port.out.NotificationPort;
import salon.billing.domain.model.document.AccountingDocument;

import java.util.logging.Logger;

public class NotificationAdapter implements NotificationPort {

    private static final Logger LOGGER = Logger.getLogger(NotificationAdapter.class.getName());

    @Override
    public void notifyInvoiceIssued(AccountingDocument document, byte[] pdf) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        if (pdf == null || pdf.length == 0) {
            throw new IllegalArgumentException("PDF content cannot be empty");
        }

        String recipientName = document.getBuyer().name();
        String documentId = document.getId().value();

        LOGGER.info(String.format(
                "Sending invoice email. To: %s, Document ID: %s, PDF attachment size: %d bytes",
                recipientName,
                documentId,
                pdf.length
        ));
    }
}