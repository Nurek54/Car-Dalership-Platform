package salon.billing.infrastructure.out.mock;

import salon.billing.application.port.out.NotificationGeneration;
import salon.billing.application.domain.model.document.AccountingDocument;
import salon.common.model.Money;

import java.util.logging.Logger;

/**
 * Adapter wyjściowy portu NotificationGeneration — symuluje wysyłkę e-maili (zamiast SMTP loguje).
 * W środowisku docelowym zastępuje go adapter SMTP wstrzykiwany w pierścieniu infrastruktury.
 */
public class NotificationAdapter implements NotificationGeneration {

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

    @Override
    public void notifyPaymentReminder(AccountingDocument document, Money outstandingBalance) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        if (outstandingBalance == null) {
            throw new IllegalArgumentException("Outstanding balance cannot be null");
        }

        LOGGER.info(String.format(
                "Sending payment reminder email. To: %s, Transfer title: %s, Outstanding: %s %s, Due: %s",
                document.getBuyer().name(),
                document.getInvoiceTitle(),
                outstandingBalance.amount(),
                outstandingBalance.currency(),
                document.getDueDate()
        ));
    }
}
