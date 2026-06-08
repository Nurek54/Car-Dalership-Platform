package salon.financing.application.port.in;

/**
 * Port wejściowy: obsługa wniosku finansowego (UC-FIN-01).
 */
public interface ProcessFinancingUseCase {
    String submitFinancing(String orderId, String customerId, java.math.BigDecimal amount, String currency);
    void processDecision(String applicationId);
}
