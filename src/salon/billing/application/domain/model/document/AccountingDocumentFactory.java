package salon.billing.application.domain.model.document;

import salon.common.model.Money;
import salon.common.model.OrderId;

/**
 * FABRYKA (Rys. 48 — AccountingDocumentFactory): hermetyzuje tworzenie agregatu
 * {@link AccountingDocument}. Deleguje do metody wytwórczej korzenia
 * ({@link AccountingDocument#createInvoice}) — klient (usługa aplikacji) nie zna algorytmu budowy
 * ani reguł terminu płatności. Operacja atomowa, zwraca dokument w stanie DRAFT.
 */
public class AccountingDocumentFactory {

    public AccountingDocument createInvoice(OrderId orderId, BuyerDetails buyer, SellerDetails seller,
                                            Money totalAmount, String invoiceTitle, String authorizedIssuer) {
        return AccountingDocument.createInvoice(
                orderId, buyer, seller, totalAmount, invoiceTitle, authorizedIssuer);
    }
}
