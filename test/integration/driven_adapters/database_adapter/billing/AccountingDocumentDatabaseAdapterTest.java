package integration.driven_adapters.database_adapter.billing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import salon.billing.domain.model.document.AccountingDocument;
import salon.billing.domain.model.document.BuyerDetails;
import salon.billing.domain.model.document.DocumentId;
import salon.billing.domain.model.document.DocumentStatus;
import salon.billing.domain.model.document.SellerDetails;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(AccountingDocumentDatabaseAdapter.class)
class AccountingDocumentDatabaseAdapterTest {

    @Autowired
    private AccountingDocumentDatabaseAdapter adapter;

    // 1. ZAPIS, ODCZYT I MAPOWANIE: agregat z obiektami wartości (BuyerDetails, SellerDetails, Money)
    @Test
    void shouldSaveAndRetrieveAccountingDocumentWithValueObjectsAndStatus() {
        AccountingDocument document = AccountingDocument.createInvoice(
                new OrderId("ORD-555"),
                new BuyerDetails("Firma Kowalski", "1234567890"),
                new SellerDetails("Salon Samochodowy Sp. z o.o.", "5260000000"),
                Money.of(new BigDecimal("400.00"), "PLN"),
                "Faktura koncowa ORD-555",
                "ksiegowy@salon.pl");
        document.markAsIssued();

        adapter.save(document);
        Optional<AccountingDocument> retrievedDoc = adapter.findById(document.getId());

        assertThat(retrievedDoc).isPresent();
        AccountingDocument retrieved = retrievedDoc.get();
        assertThat(retrieved.getStatus()).isEqualTo(DocumentStatus.ISSUED);
        assertThat(retrieved.getBuyer().nip()).isEqualTo("1234567890");
        assertThat(retrieved.getBuyer().isCorporate()).isTrue();
        assertThat(retrieved.getTotalAmount().getAmount()).isEqualByComparingTo("400.00");
        // Podmiot gospodarczy -> 14-dniowy termin płatności.
        assertThat(retrieved.getDueDate()).isEqualTo(retrieved.getIssueDate().plusDays(14));
    }

    // 2. BRAK DANYCH
    @Test
    void shouldReturnEmptyOptionalWhenDocumentDoesNotExist() {
        Optional<AccountingDocument> result = adapter.findById(new DocumentId("DOC-UNKNOWN"));
        assertThat(result).isEmpty();
    }
}
