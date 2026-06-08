package salon.billing.domain.model.document;

import salon.shared.model.Money;
import salon.shared.model.OrderId;

/**
 * Bezpieczna fabryka dokumentu (UC-FIR-01 / UC-FIR-02).
 *
 * Otrzymuje gotową kwotę wyliczoną przez InvoiceCalculationDomainService i tworzy poprawny
 * agregat AccountingDocument. Walidacja polityki terminów płatności (dueDate) zamknięta jest
 * wewnątrz domeny — fabryka korzysta z AccountingDocument.createInvoice, które ustala termin
 * na podstawie BuyerDetails.isCorporate().
 */
public class AccountingDocumentFactory {

    public AccountingDocument create(OrderId orderId,
                                     BuyerDetails buyer,
                                     SellerDetails seller,
                                     Money amount,
                                     String invoiceTitle,
                                     String authorizedIssuer) {
        return AccountingDocument.createInvoice(
                orderId, buyer, seller, amount, invoiceTitle, authorizedIssuer);
    }
}
