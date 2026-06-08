package salon.billing.application.port.out;

import salon.billing.domain.model.document.AccountingDocument;

/**
 * Port wyjściowy: powiadomienia (np. e-mail/SMTP) o wystawionym dokumencie.
 * Konkretny adapter wstrzykiwany w pierścieniu infrastruktury.
 */
public interface NotificationPort {
    void notifyInvoiceIssued(AccountingDocument document, byte[] pdf);
}
