package salon.financing.application.port.in;

/**
 * Port wejściowy (driving) — wydanie decyzji finansowej (UC-FIN-02), zgodnie z PDF rozdz. 3.6.3.
 *
 * Oddzielony od ProcessFinancing (UC-FIN-01): proces po złożeniu wniosku usypia,
 * a ten port wybudza go, gdy z banku przyjdzie decyzja (ręcznie przez analityka —
 * FinancingDecisionRestController, lub asynchronicznie z ACL banku).
 */
public interface IssueDecisionUseCase {

    /** UC-FIN-02: przetworzenie decyzji banku dla wskazanego wniosku. */
    void processDecision(String applicationId);
}
