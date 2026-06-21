package salon.billing.application.domain.model.document;

import salon.common.model.Money;
import salon.common.model.OrderId;

/**
 * FACTORY (Fig. 48 — AccountingDocumentFactory): encapsulates the creation of the aggregate
 * {@link AccountingDocument}. Delegates to the root's factory method
 * ({@link AccountingDocument#createInvoice}) — the client (application service) does not know the construction algorithm
 * nor the due-date rules. An atomic operation, returns a document in the DRAFT state.
 */
public class AccountingDocumentFactory {

    public AccountingDocument createInvoice(OrderId orderId, BuyerDetails buyer, SellerDetails seller,
                                            Money totalAmount, String invoiceTitle, String authorizedIssuer) {
        return AccountingDocument.createInvoice(
                orderId, buyer, seller, totalAmount, invoiceTitle, authorizedIssuer);
    }
}
