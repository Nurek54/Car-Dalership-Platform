package salon.billing.application.port.out;

import salon.billing.application.domain.model.document.AccountingDocument;
import salon.common.model.Money;
import salon.common.model.OrderId;

/**
 * PORT WYJSCIOWY (Rys. 48 — NotificationGeneration) — powiadomienia do klienta.
 *
 * UC-FIR-01/02: e-mail z danymi do przelewu i wystawiona faktura; dodatkowo cykliczne
 * przypomnienia o niezaplaconym saldzie (UC-FIR-03).
 */
public interface NotificationGeneration {

    /** Wyslanie do klienta wystawionego dokumentu (faktura/proforma) z danymi do przelewu. */
    void notifyInvoiceIssued(AccountingDocument document, byte[] pdf);

    /** Przypomnienie o niezaplaconym saldzie zamowienia. */
    void notifyPaymentReminder(OrderId orderId, Money outstanding);
}
