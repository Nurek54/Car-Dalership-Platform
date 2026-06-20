package salon.billing.application.port.out;

import salon.billing.application.domain.model.document.BuyerDetails;

/**
 * PORT WYJSCIOWY (Rys. 48 — SalesIntegration, ACL) — synchroniczne zapytanie (Query) do Kontekstu
 * Sprzedazy i CRM o dane nabywcy potrzebne na fakturze (UC-FIR-01/02).
 *
 * Zdarzenie wyzwalajace niesie tylko orderId (zgodnosc z RODO) — dane nabywcy dociaga ten port po
 * orderId. Kontrakt wyrazony w typie lokalnym Fakturowania (BuyerDetails), tlumaczonym z migawki
 * Sprzedazy przez adapter (Warstwa Zapobiegajaca Uszkodzeniu).
 */
public interface SalesIntegration {

    BuyerDetails getBuyerDetails(String orderId);
}
