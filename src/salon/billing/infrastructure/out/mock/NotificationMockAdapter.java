package salon.billing.infrastructure.out.mock;

import salon.billing.application.domain.model.document.AccountingDocument;
import salon.billing.application.port.out.NotificationGeneration;
import salon.common.model.Money;
import salon.common.model.OrderId;

/**
 * ADAPTER WYJSCIOWY (Rys. 48 — NotificationGenerator) — atrapa portu {@link NotificationGeneration}
 * uzywana w demach i testach. Cicha (bez I/O), umozliwia testowanie wnetrza niezaleznie od poczty.
 */
public class NotificationMockAdapter implements NotificationGeneration {

    @Override
    public void notifyInvoiceIssued(AccountingDocument document, byte[] pdf) {
        // no-op (atrapa do testow)
    }

    @Override
    public void notifyPaymentReminder(OrderId orderId, Money outstanding) {
        // no-op (atrapa do testow)
    }
}
