package integration.billing_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import salon.billing.application.domain.model.document.AccountingDocument;
import salon.billing.application.domain.model.document.BuyerDetails;
import salon.billing.application.domain.model.document.SellerDetails;
import salon.billing.application.port.out.NotificationGeneration;
import salon.billing.infrastructure.out.mock.NotificationAdapter;
import salon.common.model.Money;
import salon.common.model.OrderId;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/** Integracja adaptera powiadomień — wysyłka informacji o wystawionym dokumencie i przypomnień. */
@SpringBootTest(classes = NotificationAdapter.class)
class NotificationAdapterTest {

    @Autowired private NotificationGeneration notification;

    private AccountingDocument invoice() {
        return AccountingDocument.createInvoice(new OrderId("ORD-1"),
                new BuyerDetails("Firma S.A.", "1234563218"),
                new SellerDetails("Salon Sp. z o.o.", "5260000000"),
                Money.of(100000, "PLN"), "Faktura końcowa ORD-1", "FA-Księgowy");
    }

    @Test
    void shouldNotifyInvoiceIssuedWithoutError() {
        // Powiadomienie klienta o wystawionym dokumencie (np. e-mail) wykonuje się bez błędu
        assertDoesNotThrow(() -> notification.notifyInvoiceIssued(invoice(), new byte[]{1, 2, 3}));
    }

    @Test
    void shouldNotifyPaymentReminderWithoutError() {
        // Przypomnienie o płatności dla otwartej należności wykonuje się bez błędu
        assertDoesNotThrow(() -> notification.notifyPaymentReminder(new OrderId("ORD-1"), Money.of(60000, "PLN")));
    }
}
