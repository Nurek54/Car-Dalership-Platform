package salon.financing.application.port.out;

import salon.financing.application.domain.model.financing.BuyerDetails;
import salon.common.model.Money;
import salon.common.model.OrderId;

/**
 * Port wyjściowy (ACL) Kontekstu Finansowania — synchroniczne zapytania (Query)
 * do Kontekstu Sprzedaży i CRM o dane potrzebne do wniosku finansowego (UC-FIN-01):
 * dane nabywcy oraz cenę końcową oferty źródłowej zamówienia.
 *
 * Kontrakt wyrażony wyłącznie w typach lokalnych Finansowania ({@link BuyerDetails})
 * i Wspólnego Jądra ({@link Money}, {@link OrderId}).
 */
public interface SalesIntegration {

    /** Dane nabywcy dla zamówienia (do oceny zdolności po stronie banku). */
    BuyerDetails buyerDetails(OrderId orderId);

    /** Cena końcowa oferty źródłowej zamówienia — kwota wnioskowanego finansowania. */
    Money offerFinalPrice(OrderId orderId);
}
