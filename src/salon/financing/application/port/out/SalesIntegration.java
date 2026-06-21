package salon.financing.application.port.out;

import salon.common.model.Money;
import salon.financing.application.domain.model.financing.BuyerDetails;

/**
 * PORT WYJSCIOWY (Rysunek 42 — SalesIntegration, ACL) — synchroniczne zapytania (Query)
 * do Kontekstu Sprzedazy i CRM o dane potrzebne do wniosku finansowego (UC-FIN-01):
 * dane nabywcy oraz cene koncowa oferty zrodlowej zamowienia.
 *
 * Kontrakt wyrazony w typie lokalnym Finansowania (BuyerDetails) i Wspolnym Jadrze (Money);
 * zamowienie identyfikowane lancuchem (rozlaczny model), tlumaczonym w adapterze (ACL).
 */
public interface SalesIntegration {

    /** Dane nabywcy dla zamowienia (do oceny zdolnosci po stronie banku). */
    BuyerDetails buyerDetails(String orderId);

    /** Cena koncowa oferty zrodlowej zamowienia — kwota wnioskowanego finansowania. */
    Money offerFinalPrice(String orderId);
}
