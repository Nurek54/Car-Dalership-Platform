package integration.driven_adapters.database_adapter.billing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import salon.billing.domain.model.AccountingDocument;
import salon.billing.domain.model.DocumentId;
import salon.billing.domain.model.DocumentType;
import salon.billing.domain.model.DocumentState;
import salon.billing.domain.model.TaxDetails;
import salon.billing.domain.model.DocumentLine;
import salon.shared.model.Money;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(AccountingDocumentDatabaseAdapter.class)
class AccountingDocumentDatabaseAdapterTest {

    @Autowired
    private AccountingDocumentDatabaseAdapter adapter;

    // 1. ZAPIS, ODCZYT I MAPOWANIE: Agregat z kolekcją encji lokalnych (DocumentLine)
    @Test
    void shouldSaveAndRetrieveAccountingDocumentWithAllLinesAndKsefStatus() {
        // Arrange
        DocumentId docId = new DocumentId("DOC-FV-001");
        TaxDetails buyerTaxDetails = new TaxDetails("Firma Kowalski", "1234567890");

        AccountingDocument document = new AccountingDocument(
                docId,
                DocumentType.VAT_INVOICE,
                buyerTaxDetails
        );

        // Dodajemy lokalne encje (pozycje na fakturze) do głównego agregatu
        document.addLine("Wymiana opon", Money.of(new BigDecimal("250.00"), "PLN"));
        document.addLine("Olej silnikowy", Money.of(new BigDecimal("150.00"), "PLN"));

        // Zmieniamy stan agregatu - symulacja oczekiwania na API KSeF
        document.markAsKsefPending();

        // Act - Adapter musi zapisać nie tylko główną tabelę dokumentu, ale również tabele powiązane
        adapter.save(document);

        // Odczyt agregatu z bazy
        Optional<AccountingDocument> retrievedDoc = adapter.findById(docId);

        // Assert
        assertThat(retrievedDoc).isPresent();
        AccountingDocument retrieved = retrievedDoc.get();

        // Weryfikacja głównych danych
        assertThat(retrieved.getId()).isEqualTo(docId);
        assertThat(retrieved.getType()).isEqualTo(DocumentType.VAT_INVOICE);
        assertThat(retrieved.getState()).isEqualTo(DocumentState.PENDING_KSEF);

        // Weryfikacja obiektu zagnieżdżonego (TaxDetails)
        assertThat(retrieved.getTaxDetails().getNip()).isEqualTo("1234567890");

        // KLUCZOWA ASERCJA: Czy relacje Hibernate (np. @OneToMany + Cascade) poprawnie zapisały pozycje faktury?
        assertThat(retrieved.getLines())
                .hasSize(2)
                .extracting(DocumentLine::getDescription)
                .containsExactlyInAnyOrder("Wymiana opon", "Olej silnikowy");

        // Weryfikacja, czy agregat poprawnie wyliczył totalAmount na podstawie pozycji (400.00 PLN)
        assertThat(retrieved.getTotalAmount().getAmount()).isEqualByComparingTo("400.00");
    }

    // 2. BRAK DANYCH
    @Test
    void shouldReturnEmptyOptionalWhenDocumentDoesNotExist() {
        // Act
        Optional<AccountingDocument> result = adapter.findById(new DocumentId("DOC-UNKNOWN"));

        // Assert
        assertThat(result).isEmpty();
    }
}