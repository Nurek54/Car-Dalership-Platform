package salon.billing.domain.model.document;

import salon.billing.domain.event.InvoiceCreatedEvent;
import salon.shared.event.AbstractAggregateRoot;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Aggregate Root: dokument księgowy (faktura / dokument zadatku) — UC-FIR-02.
 *
 * Oddzielony od agregatu Settlement, co pozwala na niezależne wersjonowanie dokumentów i unika
 * blokad bazy podczas jednoczesnej rejestracji wpłaty i wystawiania faktury.
 *
 * Niezmienniki:
 *  - dokument powstaje wyłącznie przez statyczną fabrykę {@link #createInvoice},
 *  - kwota (totalAmount) jest gotowym obiektem wartości wyliczonym poza agregatem
 *    (InvoiceCalculationDomainService) — agregat jej nie przelicza,
 *  - termin płatności (dueDate) wynika z polityki firmy: 7 dni dla osób fizycznych,
 *    14 dni dla podmiotów gospodarczych (na podstawie BuyerDetails.isCorporate()),
 *  - dokument w stanie ISSUED jest "zamrożony".
 */
public class AccountingDocument extends AbstractAggregateRoot {

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

    private AccountingDocument(DocumentId id,
                              OrderId orderId,
                              String invoiceTitle,
                              BuyerDetails buyer,
                              SellerDetails seller,
                              Money totalAmount,
                              LocalDate issueDate,
                              LocalDate dueDate,
                              String authorizedIssuer) {
        this.id = id;
        this.orderId = orderId;
        this.invoiceTitle = invoiceTitle;
        this.buyer = buyer;
        this.seller = seller;
        this.totalAmount = totalAmount;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.authorizedIssuer = authorizedIssuer;
        this.status = DocumentStatus.DRAFT;
    }

    /**
     * Statyczna fabryka dokumentu. Wylicza termin płatności na podstawie typu nabywcy
     * i rejestruje zdarzenie InvoiceCreated.
     */
    public static AccountingDocument createInvoice(OrderId orderId,
                                                   BuyerDetails buyer,
                                                   SellerDetails seller,
                                                   Money totalAmount,
                                                   String invoiceTitle,
                                                   String authorizedIssuer) {
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

        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(
                buyer.isCorporate() ? DUE_DAYS_CORPORATE : DUE_DAYS_INDIVIDUAL);

        AccountingDocument document = new AccountingDocument(
                DocumentId.generate(), orderId, invoiceTitle, buyer, seller,
                totalAmount, issueDate, dueDate, authorizedIssuer);

        document.registerEvent(new InvoiceCreatedEvent(
                UUID.randomUUID(), document.id.value(), orderId.value(), Instant.now()));
        return document;
    }

    /**
     * Generuje reprezentację PDF dokumentu. Tutaj zwracamy prostą reprezentację bajtową;
     * faktyczne renderowanie deleguje warstwa aplikacji do PdfGeneratorPort.
     */
    public byte[] generatePdf() {
        String content = "INVOICE " + this.id.value()
                + " | title=" + this.invoiceTitle
                + " | buyer=" + this.buyer.name()
                + " | seller=" + this.seller.name()
                + " | amount=" + this.totalAmount.amount() + " " + this.totalAmount.currency()
                + " | issued=" + this.issueDate + " | due=" + this.dueDate
                + " | issuer=" + this.authorizedIssuer;
        return content.getBytes(StandardCharsets.UTF_8);
    }

    // Przejście DRAFT -> ISSUED.
    public void markAsIssued() {
        if (this.status == DocumentStatus.ISSUED) {
            throw new IllegalStateException("Document is already issued.");
        }
        if (this.status == DocumentStatus.ERROR) {
            throw new IllegalStateException("Cannot issue a document in ERROR state.");
        }
        this.status = DocumentStatus.ISSUED;
    }

    // Oznaczenie błędu przetwarzania (np. awaria generatora PDF / notyfikacji).
    public void markAsError() {
        this.status = DocumentStatus.ERROR;
    }

    public DocumentId getId() {
        return this.id;
    }

    public OrderId getOrderId() {
        return this.orderId;
    }

    public String getInvoiceTitle() {
        return this.invoiceTitle;
    }

    public BuyerDetails getBuyer() {
        return this.buyer;
    }

    public SellerDetails getSeller() {
        return this.seller;
    }

    public Money getTotalAmount() {
        return this.totalAmount;
    }

    public LocalDate getIssueDate() {
        return this.issueDate;
    }

    public LocalDate getDueDate() {
        return this.dueDate;
    }

    public String getAuthorizedIssuer() {
        return this.authorizedIssuer;
    }

    public DocumentStatus getStatus() {
        return this.status;
    }
}
