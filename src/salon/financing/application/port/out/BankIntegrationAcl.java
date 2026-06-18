package salon.financing.application.port.out;

/**
 * Port wyjściowy (driven, ACL) dwukierunkowej integracji z systemem banku —
 * węzeł "BankIntegrationAcl" w docs/Architecture/FinancingArchitecture.md
 * (PDF rozdz. 3.6.3). Warstwa tłumacząca izoluje jądro od formatów API banku.
 */
public interface BankIntegrationAcl {

    /** UC-FIN-01, krok 3-4: wysłanie przetłumaczonego wniosku do systemu banku. */
    void submitApplication(String applicationId, String customerId);

    /** UC-FIN-02: odczyt decyzji banku dla wniosku (po FinancingDecisionReceivedFromBank). */
    boolean isApproved(String applicationId);

    /**
     * UC-CRM-03 -> UC-FIN-01: uruchomienie procesu weryfikacji zdolności kredytowej
     * dla zamówienia (reakcja na FinancingRequestedEvent z CRM). Domyślna implementacja
     * deleguje do submitApplication po stronie adaptera.
     */
    default void startCreditCheckProcess(String orderId) {
        submitApplication("FIN-APP-" + orderId, orderId);
    }
}
