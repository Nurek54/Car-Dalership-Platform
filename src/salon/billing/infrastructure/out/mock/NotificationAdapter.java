package salon.billing.infrastructure.out.mock;

import salon.billing.application.domain.model.document.AccountingDocument;
import salon.billing.application.port.out.NotificationGeneration;
import salon.common.model.Money;
import salon.common.model.OrderId;

/**
 * OUTBOUND ADAPTER (Fig. 48 — NotificationGenerator) — a mock of the {@link NotificationGeneration} port
 * used in the production run (Composition Root). Simulates sending an e-mail to the customer.
 */
public class NotificationAdapter implements NotificationGeneration {

    @Override
    public void notifyInvoiceIssued(AccountingDocument document, byte[] pdf) {
        System.out.println("[NotificationAdapter] E-mail to the buyer of order "
                + document.orderId().value() + ": dokument " + document.id().value()
                + " (due date " + document.dueDate() + ").");
    }

    @Override
    public void notifyPaymentReminder(OrderId orderId, Money outstanding) {
        System.out.println("[NotificationAdapter] Reminder for order " + orderId.value()
                + ": do zaplaty " + outstanding.getAmount().toPlainString() + " " + outstanding.currency() + ".");
    }
}
