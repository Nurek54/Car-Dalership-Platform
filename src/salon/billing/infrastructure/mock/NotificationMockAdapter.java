package salon.billing.infrastructure.mock;

import salon.billing.application.port.out.NotificationPort;
import salon.billing.domain.model.document.AccountingDocument;
import salon.shared.model.Money;

// Udawana notyfikacja (zamiast SMTP): tylko log na konsolę.
public class NotificationMockAdapter implements NotificationPort {

    @Override
    public void notifyInvoiceIssued(AccountingDocument document, byte[] pdf) {
        System.out.println("[NotificationMock] Invoice " + document.getId().value()
                + " sent to " + document.getBuyer().name() + ".");
    }

    @Override
    public void notifyPaymentReminder(AccountingDocument document, Money outstandingBalance) {
        System.out.println("[NotificationMock] Payment reminder for order "
                + document.getOrderId().value() + " (" + document.getInvoiceTitle() + "): "
                + outstandingBalance.amount() + " " + outstandingBalance.currency()
                + " outstanding, due " + document.getDueDate() + ".");
    }
}
