package salon.billing.application.domain.model.document;

import salon.common.model.Money;
import salon.common.model.OrderId;

public class AccountingDocumentFactory {

    public AccountingDocument createInvoice(OrderId orderId, BuyerDetails buyer, SellerDetails seller,
                                            Money totalAmount, String invoiceTitle, String authorizedIssuer) {
        return AccountingDocument.createInvoice(
                orderId, buyer, seller, totalAmount, invoiceTitle, authorizedIssuer);
    }
}
