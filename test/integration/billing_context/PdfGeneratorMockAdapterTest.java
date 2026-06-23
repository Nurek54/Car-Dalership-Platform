package integration.billing_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import salon.billing.application.domain.model.document.AccountingDocument;
import salon.billing.application.domain.model.document.BuyerDetails;
import salon.billing.application.domain.model.document.SellerDetails;
import salon.billing.application.port.out.PdfGeneration;
import salon.billing.infrastructure.out.mock.PdfGeneratorMockAdapter;
import salon.common.model.Money;
import salon.common.model.OrderId;

import static org.assertj.core.api.Assertions.assertThat;

/** Integracja adaptera generowania PDF — transformacja dokumentu na strumień binarny. */
@SpringBootTest(classes = PdfGeneratorMockAdapter.class)
class PdfGeneratorMockAdapterTest {

    @Autowired private PdfGeneration pdfGeneration;

    @Test
    void shouldRenderNonEmptyPdf() {
        AccountingDocument document = AccountingDocument.createInvoice(new OrderId("ORD-1"),
                new BuyerDetails("Firma S.A.", "1234563218"),
                new SellerDetails("Salon Sp. z o.o.", "5260000000"),
                Money.of(100000, "PLN"), "Faktura końcowa ORD-1", "FA-Księgowy");

        byte[] pdf = pdfGeneration.generatePdf(document);

        assertThat(pdf).isNotEmpty();
    }
}
