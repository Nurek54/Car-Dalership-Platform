package salon.sales.infrastructure.out.integration;

import salon.sales.application.port.out.CatalogIntegration;

public class CatalogIntegrationAdapter implements CatalogIntegration {

    @Override
    public void initiateConfiguratorSession(String sessionId, int modelYear) {
        System.out.println("[CatalogIntegrationAdapter] Configurator session " + sessionId
                + " opened in the Catalog for model year " + modelYear + ".");
    }
}
