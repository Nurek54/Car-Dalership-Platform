import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class AccountingDocumentTest {

    @Test
    void shouldSuccessfullyAddDocumentLineWhenInDraftState() {
        // Arrange (Given)
        AccountingDocument document = new AccountingDocument(
                new DocumentId("DOC-100"),
                DocumentType.VAT_INVOICE,
                new TaxDetails("Jan Kowalski", "1234567890")
        );

        DocumentLine newService = new DocumentLine(
                new LineId(1L),
                "Wymiana filtrów",
                Money.of(new BigDecimal("300.00"), "PLN")
        );

        // Act (When)
        document.addLineItem(newService);

        // Assert (Then)
        // Oczekujemy, że pozycja zostanie dodana do dokumentu
        assertThat(document.getLines()).contains(newService);
        // Możemy też sprawdzić, czy kwota całkowita (totalAmount) zaktualizowała się poprawnie
        assertThat(document.getTotalAmount().getAmount()).isEqualByComparingTo("300.00");
    }

    @Test
    void shouldThrowExceptionWhenTryingToModifyIssuedDocument() {
        // Arrange (Given)
        AccountingDocument document = new AccountingDocument(
                new DocumentId("DOC-101"),
                DocumentType.VAT_INVOICE,
                new TaxDetails("Firma XYZ", "9876543210")
        );

        // Symulujemy przejście przez cykl życia KSeF
        document.markAsKsefPending();
        document.confirmKsefRegistration("KSEF-REF-123456789");

        // Upewniamy się, że dokument jest wystawiony
        assertThat(document.getState()).isEqualTo(DocumentState.ISSUED);

        DocumentLine lateService = new DocumentLine(
                new LineId(2L),
                "Spóźniona usługa",
                Money.of(new BigDecimal("150.00"), "PLN")
        );

        // Act & Assert (When & Then)
        // Agregat musi wyrzucić błąd - jakiekolwiek późniejsze modyfikacje wymuszają wystawienie nowego agregatu w postaci faktury korygującej
        assertThatThrownBy(() -> document.addLineItem(lateService))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot modify document in ISSUED state");
    }

    @Test
    void shouldTransitionStateCorrectlyDuringKsefRegistration() {
        // Arrange
        AccountingDocument document = new AccountingDocument(
                new DocumentId("DOC-102"),
                DocumentType.RECEIPT,
                new TaxDetails("Osoba Fizyczna", null)
        );

        // Act & Assert - krok po kroku
        assertThat(document.getState()).isEqualTo(DocumentState.DRAFT);

        document.markAsKsefPending();
        assertThat(document.getState()).isEqualTo(DocumentState.PENDING_KSEF);

        document.confirmKsefRegistration("KSEF-REF-999");
        assertThat(document.getState()).isEqualTo(DocumentState.ISSUED);
    }
}