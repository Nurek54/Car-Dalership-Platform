package salon.billing.application.domain.model.document;

import salon.common.model.Money;
import salon.common.model.OrderId;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * KORZEŃ AGREGATU (Diagram klas — «AggregateRoot» AccountingDocument) — faktura / dokument księgowy.
 *
 * Tworzony metodą wytwórczą {@link #createInvoice} (statyczna, zgodnie z diagramem), która gwarantuje
 * niezmienniki: kompletne dane stron, niepusty tytuł, termin płatności wyliczony z
 * {@link BuyerDetails#isCorporate()}. Korzeń odpowiada za przejścia statusu (DRAFT -> ISSUED / ERROR)
 * oraz wygenerowanie reprezentacji PDF ({@link #generatePdf()}). Odwołanie do zamówienia rozłączne
 * (wyłącznie przez {@link OrderId}).
 */
public class AccountingDocument {

    private static final int DUE_DAYS_INDIVIDUAL = 7;
    private static final int DUE_DAYS_CORPORATE = 14;

    private final DocumentId id;
    private final OrderId orderId;
    private final String invoiceTitle;
    private final BuyerDetails buyer;
    private final SellerDetails seller;
    private final Money totalAmount;
    private final LocalDate issueDate;
    private final LocalDate dueDate;
    private final String authorizedIssuer;
    private DocumentStatus status;

    private AccountingDocument(DocumentId id, OrderId orderId, String invoiceTitle, BuyerDetails buyer,
                              SellerDetails seller, Money totalAmount, LocalDate issueDate,
                              LocalDate dueDate, String authorizedIssuer, DocumentStatus status) {
        this.id = id;
        this.orderId = orderId;
        this.invoiceTitle = invoiceTitle;
        this.buyer = buyer;
        this.seller = seller;
        this.totalAmount = totalAmount;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.authorizedIssuer = authorizedIssuer;
        this.status = status;
    }

    /**
     * METODA WYTWÓRCZA (Diagram klas — createInvoice$): tworzy fakturę w stanie DRAFT.
     * Atomowa, waliduje wszystkie nieopcjonalne elementy, nigdy nie zwraca niepoprawnego obiektu.
     * Termin płatności (dueDate) zależny od typu nabywcy (UC-FIR-01/02).
     */
    public static AccountingDocument createInvoice(OrderId orderId, BuyerDetails buyer,
                                                   SellerDetails seller, Money totalAmount,
                                                   String invoiceTitle, String authorizedIssuer) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        if (buyer == null) {
            throw new IllegalArgumentException("buyer must not be null.");
        }
        if (seller == null) {
            throw new IllegalArgumentException("seller must not be null.");
        }
        if (totalAmount == null) {
            throw new IllegalArgumentException("totalAmount must not be null.");
        }
        if (invoiceTitle == null || invoiceTitle.isBlank()) {
            throw new IllegalArgumentException("invoiceTitle must not be blank.");
        }
        if (authorizedIssuer == null || authorizedIssuer.isBlank()) {
            throw new IllegalArgumentException("authorizedIssuer must not be blank.");
        }
        LocalDate issue = LocalDate.now();
        LocalDate due = issue.plusDays(buyer.isCorporate() ? DUE_DAYS_CORPORATE : DUE_DAYS_INDIVIDUAL);
        return new AccountingDocument(DocumentId.generate(), orderId, invoiceTitle, buyer, seller,
                totalAmount, issue, due, authorizedIssuer, DocumentStatus.DRAFT);
    }

    /**
     * Generuje reprezentację PDF dokumentu (Diagram klas — generatePdf(): byte[]).
     * Tu: lekka, deterministyczna serializacja treści faktury; adapter PdfGeneration może
     * delegować do tej metody lub opakować ją bibliotecznym rendererem.
     */
    public byte[] generatePdf() {
        if (this.status == DocumentStatus.ERROR) {
            throw new IllegalStateException("Cannot render a document in ERROR state.");
        }
        String body = "FAKTURA\n"
                + "Tytuł: " + this.invoiceTitle + "\n"
                + "Nr dokumentu: " + this.id.value() + "\n"
                + "Zamówienie: " + this.orderId.value() + "\n"
                + "Sprzedawca: " + this.seller.name() + " (NIP " + this.seller.nip() + ")\n"
                + "Nabywca: " + this.buyer.name() + " (NIP " + this.buyer.nip() + ")\n"
                + "Kwota: " + this.totalAmount.getAmount().toPlainString() + " " + this.totalAmount.currency() + "\n"
                + "Data wystawienia: " + this.issueDate + "\n"
                + "Termin płatności: " + this.dueDate + "\n"
                + "Wystawił: " + this.authorizedIssuer + "\n";
        return body.getBytes(StandardCharsets.UTF_8);
    }

    /** UC-FIR-02, krok 4: dokument wystawiony (PDF przypisany do zamówienia). */
    public void markAsIssued() {
        if (this.status != DocumentStatus.DRAFT) {
            throw new salon.billing.application.domain.exception.IllegalSettlementStateException(
                    "Wystawić można tylko dokument w stanie DRAFT (aktualny: " + this.status + ").");
        }
        this.status = DocumentStatus.ISSUED;
    }

    /** UC-FIR-02 / A1: oznaczenie błędu generowania dokumentu. */
    public void markAsError() {
        this.status = DocumentStatus.ERROR;
    }

    public DocumentId id() {
        return id;
    }

    public OrderId orderId() {
        return orderId;
    }

    public String invoiceTitle() {
        return invoiceTitle;
    }

    public BuyerDetails buyer() {
        return buyer;
    }

    public SellerDetails seller() {
        return seller;
    }

    public Money totalAmount() {
        return totalAmount;
    }

    public LocalDate issueDate() {
        return issueDate;
    }

    public LocalDate dueDate() {
        return dueDate;
    }

    public String authorizedIssuer() {
        return authorizedIssuer;
    }

    public DocumentStatus status() {
        return status;
    }
}
