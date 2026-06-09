package salon.financing.application.port.in;

/**
 * Port wejściowy: obsługa wniosku finansowego (UC-FIN-01).
 */
public interface ProcessFinancingUseCase {
    String submitFinancing(String orderId, String customerId);
    void processDecision(String applicationId);
}
