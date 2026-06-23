package salon.sales.application.port.out;

public interface CatalogIntegration {

    
    void initiateConfiguratorSession(String sessionId, int modelYear);

    
    void openConfiguratorInterface(String sessionId, String customerId, String salespersonId);
}
