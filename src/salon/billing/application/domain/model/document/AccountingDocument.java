package salon.billing.application.domain.model.document;

import salon.common.model.Money;
import salon.common.model.OrderId;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

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

    
    public byte[] generatePdf() {
        if (this.status == DocumentStatus.ERROR) {
            throw new IllegalStateException("Cannot render a document in ERROR state.");
        }
        String body = "INVOICE\n"
                + "Title: " + this.invoiceTitle + "\n"
                + "Nr dokumentu: " + this.id.value() + "\n"
                + "Order: " + this.orderId.value() + "\n"
                + "Sprzedawca: " + this.seller.name() + " (NIP " + this.seller.nip() + ")\n"
                + "Buyer: " + this.buyer.name() + " (tax ID " + this.buyer.nip() + ")\n"
                + "Kwota: " + this.totalAmount.amount().toPlainString() + " " + this.totalAmount.currency() + "\n"
                + "Issue date: " + this.issueDate + "\n"
                + "Due date: " + this.dueDate + "\n"
                + "Issued by: " + this.authorizedIssuer + "\n";
        return body.getBytes(StandardCharsets.UTF_8);
    }

    
    public void markAsIssued() {
        if (this.status != DocumentStatus.DRAFT) {
            throw new salon.billing.application.domain.exception.IllegalSettlementStateException(
                    "Only a document in the DRAFT state can be issued (current: " + this.status + ").");
        }
        this.status = DocumentStatus.ISSUED;
    }

    
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
