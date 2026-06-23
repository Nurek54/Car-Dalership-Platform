package salon.financing.application.port.out;

public interface BankIntegrationAcl {

    
    void submitFinancingApplication(String orderId);

    
    default void startCreditCheckProcess(String orderId) {
        submitFinancingApplication(orderId);
    }
}
