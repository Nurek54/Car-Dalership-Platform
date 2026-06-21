package salon.billing.application.port.out;

import salon.billing.application.domain.model.document.AccountingDocument;
import salon.common.model.Money;
import salon.common.model.OrderId;

/**
 * OUTBOUND PORT (Fig. 48 — NotificationGeneration) — notifications to the customer.
 *
 * UC-FIR-01/02: an e-mail with transfer details and the issued invoice; additionally periodic
 * reminders about the unpaid balance (UC-FIR-03).
 */
public interface NotificationGeneration {

    /** Sends the issued document (invoice/proforma) with transfer details to the customer. */
    void notifyInvoiceIssued(AccountingDocument document, byte[] pdf);

    /** Reminder about the order's unpaid balance. */
    void notifyPaymentReminder(OrderId orderId, Money outstanding);
}
