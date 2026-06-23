package salon.financing.application.port.in;

public interface ProcessFinancing {

    
    void requestFinancing(String orderId, String customerId);

    
    void processBankDecision(String orderId, boolean approved);
}
