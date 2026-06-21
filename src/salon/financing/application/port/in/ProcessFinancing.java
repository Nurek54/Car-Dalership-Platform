package salon.financing.application.port.in;

/**
 * PORT WEJSCIOWY (Rysunek 42 — ProcessFinancing) — jedyny port wejsciowy Kontekstu Finansowania.
 *
 * Realizuje caly proces (zgodnie z diagramem — jeden port, nie osobny port decyzji):
 *  - UC-FIN-01 (requestFinancing) — zlozenie wniosku o finansowanie (trigger: FinancingRequested z CRM),
 *  - UC-FIN-02 (processBankDecision) — przetworzenie asynchronicznej decyzji banku.
 *
 * Salon nie podejmuje decyzji kredytowej — odzwierciedla jedynie status nadany przez bank.
 */
public interface ProcessFinancing {

    /** UC-FIN-01: zlozenie wniosku o finansowanie dla zamowienia. */
    void requestFinancing(String orderId, String customerId);

    /** UC-FIN-02: zarejestrowanie decyzji banku (pozytywnej/negatywnej) dla zamowienia. */
    void processBankDecision(String orderId, boolean approved);
}
