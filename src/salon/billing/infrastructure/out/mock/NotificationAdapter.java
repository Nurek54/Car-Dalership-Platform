package salon.billing.infrastructure.out.mock;

import salon.billing.application.domain.model.document.AccountingDocument;
import salon.billing.application.port.out.NotificationGeneration;
import salon.common.model.Money;
import salon.common.model.OrderId;

/**
 * ADAPTER WYJSCIOWY (Rys. 48 — NotificationGenerator) — atrapa portu {@link NotificationGeneration}
 * uzywana w uruchomieniu produkcyjnym (Composition Root). Symuluje wysylke e-mail do klienta.
 */
public class NotificationAdapter implements NotificationGeneration {

    @Override
    public void notifyInvoiceIssued(AccountingDocument document, byte[] pdf) {
        System.out.println("[NotificationAdapter] E-mail do nabywcy zamowienia "
                + document.getOrderId().value() + ": dokument " + document.getId().value()
                + " (termin platnosci " + document.getDueDate() + ").");
    }

    @Override
    public void notifyPaymentReminder(OrderId orderId, Money outstanding) {
        System.out.println("[NotificationAdapter] Przypomnienie dla zamowienia " + orderId.value()
                + ": do zaplaty " + outstanding.getAmount().toPlainString() + " " + outstanding.currency() + ".");
    }
}
