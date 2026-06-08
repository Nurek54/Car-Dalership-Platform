package salon.billing.infrastructure.mock;

import salon.billing.application.port.out.NotificationPort;
import salon.billing.domain.model.document.AccountingDocument;

// Udawana notyfikacja (zamiast SMTP): tylko log na konsolę.
public class NotificationMockAdapter implements NotificationPort {

    @Override
    public void notifyInvoiceIssued(AccountingDocument document, byte[] pdf) {
        System.out.println("[NotificationMock] Invoice " + document.getId().value()
                + " sent to " + document.getBuyer().name() + ".");
    }
}
