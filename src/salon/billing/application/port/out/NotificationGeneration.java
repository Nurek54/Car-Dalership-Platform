package salon.billing.application.port.out;

import salon.billing.application.domain.model.document.AccountingDocument;
import salon.common.model.Money;

/**
 * Port wyjściowy: powiadomienia (np. e-mail/SMTP) do klienta — PDF rozdz. 3.7.3
 * ("NotificationGeneration i PdfGeneration"). Konkretny adapter (SMTP) wstrzykiwany
 * w zewnętrznym pierścieniu infrastruktury.
 *
 * Używany przez DocumentGenerationService (UC-FIR-01/02 — wysyłka dokumentu z danymi do
 * przelewu) oraz PaymentProcessService (WF-FIR-03 — przypomnienia o niepełnych wpłatach).
 */
public interface NotificationGeneration {

    /** UC-FIR-01/02: wysyłka wystawionego dokumentu (e-mail z PDF i danymi do przelewu). */
    void notifyInvoiceIssued(AccountingDocument document, byte[] pdf);

    /** WF-FIR-03: przypomnienie o brakującej wpłacie (dane do przelewu + saldo pozostałe). */
    void notifyPaymentReminder(AccountingDocument document, Money outstandingBalance);
}
