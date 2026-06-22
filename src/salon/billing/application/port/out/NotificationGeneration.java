package salon.billing.application.port.out;

import salon.billing.application.domain.model.document.AccountingDocument;
import salon.common.model.Money;
import salon.common.model.OrderId;

public interface NotificationGeneration {

    void notifyInvoiceIssued(AccountingDocument document, byte[] pdf);

    void notifyPaymentReminder(OrderId orderId, Money outstanding);
}
