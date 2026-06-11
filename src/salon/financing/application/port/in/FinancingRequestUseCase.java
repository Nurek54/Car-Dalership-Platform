package salon.financing.application.port.in;

/**
 * Port wejściowy: weryfikacja zdolności kredytowej/leasingowej — węzeł
 * "FinancingRequestUseCase" w docs/Architecture/FinancingArchitecture.md (PDF rozdz. 3.6.3).
 *
 * Z perspektywy biznesowej to jeden, spójny przypadek użycia, technicznie dwufazowy:
 * UC-FIN-01 (złożenie wniosku — submitFinancing) usypia proces po wysyłce do ACL banku,
 * UC-FIN-02 (przetworzenie decyzji — processDecision) wybudza go, gdy ACL odbierze
 * asynchroniczną odpowiedź banku.
 */
public interface FinancingRequestUseCase {

    /** UC-FIN-01: złożenie wniosku o finansowanie (trigger: FinancingRequestedEvent z CRM). */
    String submitFinancing(String orderId, String customerId);

    /** UC-FIN-02: przetworzenie decyzji banku (trigger: FinancingDecisionReceivedFromBank). */
    void processDecision(String applicationId);
}
