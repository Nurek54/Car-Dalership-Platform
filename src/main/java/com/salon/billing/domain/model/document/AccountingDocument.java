package main.java.com.salon.billing.domain.model.document;

import main.java.com.salon.billing.domain.model.shared.Money;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate Root: dokument księgowy (faktura VAT / paragon).
 *
 * Niezmienniki:
 *  - musi mieć co najmniej jedną pozycję (1..*),
 *  - suma (totalAmount) jest liczona z pozycji, nie podawana z zewnątrz,
 *  - stanem steruje wyłącznie maszyna stanów (metody biznesowe).
 */
public class AccountingDocument {

    private final DocumentId id;
    private final DocumentType type;
    private final TaxDetails taxDetails;
    private final List<DocumentLine> lines;
    private final Money totalAmount;

    private DocumentState state;
    private String ksefReference; // null, dopóki KSeF nie potwierdzi rejestracji

    public AccountingDocument(DocumentId id,
                              DocumentType type,
                              TaxDetails taxDetails,
                              List<DocumentLine> lines) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null.");
        }
        if (taxDetails == null) {
            throw new IllegalArgumentException("taxDetails must not be null.");
        }
        // Niezmiennik 1..* — dokument bez pozycji nie ma sensu (powiązane z UC-ROZ-02, A2).
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Document must have at least one line.");
        }

        this.id = id;
        this.type = type;
        this.taxDetails = taxDetails;
        // Kopia obronna: nikt z zewnątrz nie zmodyfikuje naszej wewnętrznej listy.
        this.lines = new ArrayList<>(lines);
        this.totalAmount = sumLines(this.lines);
        this.state = DocumentState.DRAFT;
        this.ksefReference = null;
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
