package salon.billing.application.port.out;

import salon.billing.domain.model.document.AccountingDocument;
import salon.shared.model.Money;

/**
 * Port wyjściowy: powiadomienia (np. e-mail/SMTP) do klienta — PDF rozdz. 3.7.3
 * ("NotificationPort i PdfGeneratorPort"). Konkretny adapter (SMTP) wstrzykiwany
 * w zewnętrznym pierścieniu infrastruktury.
 *
 * Używany przez DocumentAppService (UC-FIR-01/02 — wysyłka dokumentu z danymi do
 * przelewu) oraz SettlementAppService (WF-FIR-03 — przypomnienia o niepełnych wpłatach).
 */
public interface NotificationPort {

    /** UC-FIR-01/02: wysyłka wystawionego dokumentu (e-mail z PDF i danymi do przelewu). */
    void notifyInvoiceIssued(AccountingDocument document, byte[] pdf);

    /** WF-FIR-03: przypomnienie o brakującej wpłacie (dane do przelewu + saldo pozostałe). */
    void notifyPaymentReminder(AccountingDocument document, Money outstandingBalance);
}
