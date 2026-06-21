package salon.financing.application.port.out;

/**
 * PORT WYJSCIOWY (driven, ACL, Rysunek 42 — BankIntegrationAcl) — integracja z systemem banku.
 *
 * Warstwa Zapobiegajaca Uszkodzeniu izoluje jadro Finansowania od formatow API banku. Bank jest
 * jedynym decydentem kredytowym; zlozenie wniosku jest asynchroniczne, a decyzja wraca pozniej
 * osobnym zdarzeniem (UC-FIN-02).
 */
public interface BankIntegrationAcl {

    /** UC-FIN-01: wyslanie (przetlumaczonego) wniosku o finansowanie do systemu banku. */
    void submitFinancingApplication(String orderId);

    /**
     * UC-CRM-03 -> UC-FIN-01: uruchomienie sprawdzania zdolnosci kredytowej dla zamowienia
     * (alias semantyczny uzywany przez Kontekst Sprzedazy). Domyslnie deleguje do zlozenia wniosku.
     */
    default void startCreditCheckProcess(String orderId) {
        submitFinancingApplication(orderId);
    }
}
