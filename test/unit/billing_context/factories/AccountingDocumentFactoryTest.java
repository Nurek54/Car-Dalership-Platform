package unit.billing_context.factories;

import org.junit.jupiter.api.Test;
import salon.billing.application.domain.model.document.AccountingDocument;
import salon.billing.application.domain.model.document.AccountingDocumentFactory;
import salon.billing.application.domain.model.document.BuyerDetails;
import salon.billing.application.domain.model.document.DocumentStatus;
import salon.billing.application.domain.model.document.SellerDetails;
import salon.common.model.Money;
import salon.common.model.OrderId;

import static org.assertj.core.api.Assertions.*;

/** Fabryka agregatu AccountingDocument — utworzenie faktury w stanie DRAFT. */
class AccountingDocumentFactoryTest {

    private final AccountingDocumentFactory factory = new AccountingDocumentFactory();

    @Test
    void shouldCreateInvoiceInDraft() {
        AccountingDocument document = factory.createInvoice(
                new OrderId("ORD-1"),
                new BuyerDetails("Firma S.A.", "1234563218"),
                new SellerDetails("Salon Sp. z o.o.", "5260000000"),
                Money.of(100000, "PLN"), "Faktura końcowa ORD-1", "FA-Księgowy");

        assertThat(document.id()).isNotNull();
        assertThat(document.status()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(document.totalAmount()).isEqualTo(Money.of(100000, "PLN"));
    }
}
