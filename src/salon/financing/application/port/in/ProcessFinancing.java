package salon.financing.application.port.in;

/**
 * Port wejściowy: weryfikacja zdolności kredytowej/leasingowej — węzeł
 * "ProcessFinancing" w docs/Architecture/FinancingArchitecture.md (PDF rozdz. 3.6.3).
 *
 * Realizuje UC-FIN-01 (złożenie wniosku — submitFinancing): proces usypia po wysyłce
 * do ACL banku. Przetworzenie decyzji banku (UC-FIN-02) obsługuje osobny port
 * {@link IssueDecisionUseCase}.
 */
public interface ProcessFinancing {

    /** UC-FIN-01: złożenie wniosku o finansowanie (trigger: FinancingRequestedEvent z CRM). */
    String submitFinancing(String orderId, String customerId);
}
