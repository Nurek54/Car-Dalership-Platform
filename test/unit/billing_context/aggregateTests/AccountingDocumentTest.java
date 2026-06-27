package unit.billing_context.aggregateTests;

import org.junit.jupiter.api.Test;
import salon.billing.application.domain.exception.IllegalSettlementStateException;
import salon.billing.application.domain.model.document.AccountingDocument;
import salon.billing.application.domain.model.document.BuyerDetails;
import salon.billing.application.domain.model.document.DocumentStatus;
import salon.billing.application.domain.model.document.SellerDetails;
import salon.common.model.Money;
import salon.common.model.OrderId;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/** UC-FIR-01/02: Agregat AccountingDocument (dokument księgowy / faktura). */
class AccountingDocumentTest {

    private final SellerDetails seller = new SellerDetails("Salon Sp. z o.o.", "5260000000");

    private AccountingDocument invoiceFor(BuyerDetails buyer) {
        return AccountingDocument.createInvoice(new OrderId("ORD-1"), buyer, seller,
                Money.of(100000, "PLN"), "Faktura końcowa ORD-1", "FA-Księgowy");
    }

    @Test
    void shouldCreateInvoiceInDraftWithCorporateDueDate() {
        // Nabywca firmowy (NIP 10-cyfrowy) — termin płatności 14 dni
        AccountingDocument document = invoiceFor(new BuyerDetails("Firma S.A.", "1234563218"));

        assertThat(document.status()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(document.dueDate()).isEqualTo(LocalDate.now().plusDays(14));
    }

    @Test
    void shouldUseShorterDueDateForIndividual() {
        // Nabywca indywidualny (brak 10-cyfrowego NIP) — termin płatności 7 dni
        AccountingDocument document = invoiceFor(new BuyerDetails("Jan Kowalski", "PESEL-90010112345"));

        assertThat(document.dueDate()).isEqualTo(LocalDate.now().plusDays(7));
    }

    @Test
    void shouldRenderPdfAndBeIssued() {
        AccountingDocument document = invoiceFor(new BuyerDetails("Firma S.A.", "1234563218"));

        // Generowanie pliku PDF zwraca niepustą zawartość
        assertThat(document.generatePdf()).isNotEmpty();

        // Dokument przechodzi z DRAFT do ISSUED
        document.markAsIssued();
        assertThat(document.status()).isEqualTo(DocumentStatus.ISSUED);

        // Ponowne wystawienie jest blokowane
        assertThatThrownBy(document::markAsIssued)
                .isInstanceOf(IllegalSettlementStateException.class);
    }

    @Test
    void shouldRejectPdfRenderingInErrorState() { // UC-FIR-02 / A1
        AccountingDocument document = invoiceFor(new BuyerDetails("Firma S.A.", "1234563218"));

        // Błąd generowania dokumentu — dokument w stanie ERROR nie może być renderowany
        document.markAsError();
        assertThatThrownBy(document::generatePdf)
                .isInstanceOf(IllegalStateException.class);
    }
}
