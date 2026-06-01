package salon.billing.domain.model.document;

import salon.billing.domain.event.InvoiceIssuedEvent;
import salon.shared.event.AbstractAggregateRoot;
import salon.shared.model.Money;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root: dokument księgowy (faktura VAT / paragon).
 *
 * Niezmienniki:
 *  - pozycje dodajemy WYŁĄCZNIE metodą addLineItem (kontrolowany cykl życia),
 *  - suma (totalAmount) jest liczona z pozycji, nie podawana z zewnątrz,
 *  - dokumentu w stanie ISSUED nie wolno już modyfikować (korekta = nowy agregat),
 *  - stanem steruje wyłącznie maszyna stanów (metody biznesowe).
 *
 * Zdarzenia domenowe: zdarzenie FakturaWystawiona powstaje DOKŁADNIE w momencie potwierdzenia
 * rejestracji w KSeF (confirmKsefRegistration) — czyli tylko na ścieżce faktycznego wystawienia.
 */
public class AccountingDocument extends AbstractAggregateRoot {

    private final DocumentId id;
    private final DocumentType type;
    private final TaxDetails taxDetails;
    private final List<DocumentLine> lines;

    private Money totalAmount;     // null, dopóki nie dodamy pierwszej pozycji
    private DocumentState state;
    private String ksefReference;  // null, dopóki KSeF nie potwierdzi rejestracji

    public AccountingDocument(DocumentId id, DocumentType type, TaxDetails taxDetails) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null.");
        }
        if (taxDetails == null) {
            throw new IllegalArgumentException("taxDetails must not be null.");
        }
        this.id = id;
        this.type = type;
        this.taxDetails = taxDetails;
        this.lines = new ArrayList<>();
        this.totalAmount = null;
        this.state = DocumentState.DRAFT;
        this.ksefReference = null;
    }

    // Dodanie pozycji. Po wystawieniu (ISSUED) dokument jest "zamrożony".
    public void addLineItem(DocumentLine line) {
        if (line == null) {
            throw new IllegalArgumentException("line must not be null.");
        }
        if (this.state != DocumentState.DRAFT) {
            throw new IllegalStateException("Cannot modify document in " + this.state + " state.");
        }
        this.lines.add(line);
        this.totalAmount = sumLines(this.lines);
    }

    // Suma wartości pozycji — zwykła pętla for, bez Streamów.
    private Money sumLines(List<DocumentLine> documentLines) {
        Money total = null;
        for (int i = 0; i < documentLines.size(); i++) {
            Money lineCost = documentLines.get(i).getCost();
            if (total == null) {
                total = lineCost; // pierwsza pozycja nadaje walutę sumy
            } else {
                total = total.add(lineCost);
            }
        }
        return total;
    }

    // UC-ROZ-02, A1: brak odpowiedzi z KSeF -> dokument czeka na wysyłkę.
    public void markAsKsefPending() {
        if (this.state != DocumentState.DRAFT) {
            throw new IllegalStateException(
                    "Only a document in DRAFT state can be marked as PENDING_KSEF.");
        }
        this.state = DocumentState.PENDING_KSEF;
    }

    // UC-ROZ-02, krok 6: KSeF potwierdził rejestrację i zwrócił numer.
    public void confirmKsefRegistration(String ksefReference) {
        if (ksefReference == null || ksefReference.isBlank()) {
            throw new IllegalArgumentException("KSeF reference must not be blank.");
        }
        if (this.state == DocumentState.ISSUED) {
            throw new IllegalStateException("Document is already issued.");
        }
        this.ksefReference = ksefReference;
        this.state = DocumentState.ISSUED;
        registerEvent(new InvoiceIssuedEvent(
                UUID.randomUUID(), this.id.value(), this.ksefReference, Instant.now()));
    }

    public List<DocumentLine> getLines() {
        return new ArrayList<>(this.lines); // kopia obronna przy odczycie
    }

    public DocumentId getId() {
        return this.id;
    }

    public DocumentType getType() {
        return this.type;
    }

    public TaxDetails getTaxDetails() {
        return this.taxDetails;
    }

    public Money getTotalAmount() {
        return this.totalAmount;
    }

    public DocumentState getState() {
        return this.state;
    }

    public String getKsefReference() {
        return this.ksefReference;
    }
}
