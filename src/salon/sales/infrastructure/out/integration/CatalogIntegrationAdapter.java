package salon.sales.infrastructure.out.integration;

import salon.sales.application.port.out.CatalogIntegration;

public class CatalogIntegrationAdapter implements CatalogIntegration {

    @Override
    public void initiateConfiguratorSession(String sessionId, int modelYear) {
        System.out.println("[CatalogIntegrationAdapter] Configurator session " + sessionId
                + " opened in the Catalog for model year " + modelYear + ".");
    }

    @Override
    public void openConfiguratorInterface(String sessionId, String customerId, String salespersonId) {
        System.out.println("[CatalogIntegrationAdapter] Configurator interface opened for session "
                + sessionId + " (customer " + customerId + ", salesperson " + salespersonId + ").");
    }
}
