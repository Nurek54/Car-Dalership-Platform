package unit.invoicing_and_billing_context;

import org.junit.jupiter.api.Test;
import salon.billing.domain.model.document.AccountingDocument;
import salon.billing.domain.model.document.BuyerDetails;
import salon.billing.domain.model.document.DocumentStatus;
import salon.billing.domain.model.document.SellerDetails;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class AccountingDocumentTest {

    private static final SellerDetails SELLER =
            new SellerDetails("Salon Samochodowy Sp. z o.o.", "5260000000");

    @Test
    void shouldCreateInvoiceInDraftStateWithCorporateDueDate() {
        AccountingDocument document = AccountingDocument.createInvoice(
                new OrderId("ORD-123"),
                new BuyerDetails("Firma XYZ", "9876543210"),
                SELLER,
                Money.of(new BigDecimal("100000.00"), "PLN"),
                "Faktura koncowa ORD-123",
                "ksiegowy@salon.pl");

        assertThat(document.getStatus()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(document.getTotalAmount().getAmount()).isEqualByComparingTo("100000.00");
        // Podmiot gospodarczy (NIP obecny) -> 14-dniowy termin płatności.
        assertThat(document.getDueDate()).isEqualTo(document.getIssueDate().plusDays(14));
    }

    @Test
    void shouldAssignShorterDueDateForIndividualBuyer() {
        AccountingDocument document = AccountingDocument.createInvoice(
                new OrderId("ORD-124"),
                new BuyerDetails("Jan Kowalski", null), // brak NIP -> osoba fizyczna
                SELLER,
                Money.of(new BigDecimal("300.00"), "PLN"),
                "Faktura ORD-124",
                "ksiegowy@salon.pl");

        assertThat(document.getBuyer().isCorporate()).isFalse();
        // Osoba fizyczna -> 7-dniowy termin płatności.
        assertThat(document.getDueDate()).isEqualTo(LocalDate.now().plusDays(7));
    }

    @Test
    void shouldTransitionToIssuedAndThenRejectReissue() {
        AccountingDocument document = AccountingDocument.createInvoice(
                new OrderId("ORD-125"),
                new BuyerDetails("Firma XYZ", "9876543210"),
                SELLER,
                Money.of(new BigDecimal("500.00"), "PLN"),
                "Faktura ORD-125",
                "ksiegowy@salon.pl");

        document.markAsIssued();
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.ISSUED);

        assertThatThrownBy(document::markAsIssued)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already issued");
    }
}
