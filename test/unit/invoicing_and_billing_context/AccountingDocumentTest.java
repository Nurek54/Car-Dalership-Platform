package unit.invoicing_and_billing_context;

import org.junit.jupiter.api.Test;
import salon.billing.domain.model.document.AccountingDocument;
import salon.billing.domain.model.document.DocumentId;
import salon.billing.domain.model.document.DocumentLine;
import salon.billing.domain.model.document.DocumentState;
import salon.billing.domain.model.document.DocumentType;
import salon.billing.domain.model.document.LineId;
import salon.billing.domain.model.document.TaxDetails;
import salon.shared.model.Money;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class AccountingDocumentTest {

    @Test
    void shouldSuccessfullyAddDocumentLineWhenInDraftState() {
        AccountingDocument document = new AccountingDocument(
                new DocumentId("DOC-100"),
                DocumentType.VAT_INVOICE,
                new TaxDetails("Jan Kowalski", "1234567890"));

        DocumentLine newService = new DocumentLine(
                new LineId(1L), "Wymiana filtrow", Money.of(new BigDecimal("300.00"), "PLN"));

        document.addLineItem(newService);

        assertThat(document.getLines()).contains(newService);
        assertThat(document.getTotalAmount().getAmount()).isEqualByComparingTo("300.00");
    }

    @Test
    void shouldThrowExceptionWhenTryingToModifyIssuedDocument() {
        AccountingDocument document = new AccountingDocument(
                new DocumentId("DOC-101"),
                DocumentType.VAT_INVOICE,
                new TaxDetails("Firma XYZ", "9876543210"));

        document.markAsKsefPending();
        document.confirmKsefRegistration("KSEF-REF-123456789");
        assertThat(document.getState()).isEqualTo(DocumentState.ISSUED);

        DocumentLine lateService = new DocumentLine(
                new LineId(2L), "Spozniona usluga", Money.of(new BigDecimal("150.00"), "PLN"));

        assertThatThrownBy(() -> document.addLineItem(lateService))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot modify document in ISSUED state");
    }

    @Test
    void shouldTransitionStateCorrectlyDuringKsefRegistration() {
        AccountingDocument document = new AccountingDocument(
                new DocumentId("DOC-102"),
                DocumentType.RECEIPT,
                new TaxDetails("Osoba Fizyczna", null));

        assertThat(document.getState()).isEqualTo(DocumentState.DRAFT);
        document.markAsKsefPending();
        assertThat(document.getState()).isEqualTo(DocumentState.PENDING_KSEF);
        document.confirmKsefRegistration("KSEF-REF-999");
        assertThat(document.getState()).isEqualTo(DocumentState.ISSUED);
    }
}
