package salon.billing.infrastructure.out.mock;

import salon.billing.application.domain.model.document.AccountingDocument;
import salon.billing.application.port.out.NotificationGeneration;
import salon.common.model.Money;
import salon.common.model.OrderId;

/**
 * OUTBOUND ADAPTER (Fig. 48 — NotificationGenerator) — a mock of the {@link NotificationGeneration} port
 * uzywana w demach i testach. Cicha (bez I/O), umozliwia testowanie wnetrza niezaleznie od poczty.
 */
public class NotificationMockAdapter implements NotificationGeneration {

    @Override
    public void notifyInvoiceIssued(AccountingDocument document, byte[] pdf) {
        // no-op (a mock for tests)
    }

    @Override
    public void notifyPaymentReminder(OrderId orderId, Money outstanding) {
        // no-op (a mock for tests)
    }
}
