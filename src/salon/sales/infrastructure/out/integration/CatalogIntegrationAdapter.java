package salon.sales.infrastructure.out.integration;

import salon.sales.application.port.out.CatalogIntegration;

/**
 * OUTBOUND ADAPTER (ACL, Figure 22) — integration with the Catalog and Configuration Context.
 *
 * Translates the Sales request "open a configurator session" into a call on the Catalog
 * (here simulated by logging). In a distributed setup this maps onto a Catalog REST endpoint
 * or a command message.
 */
public class CatalogIntegrationAdapter implements CatalogIntegration {

    @Override
    public void initiateConfiguratorSession(String sessionId, int modelYear) {
        System.out.println("[CatalogIntegrationAdapter] Configurator session " + sessionId
                + " opened in the Catalog for model year " + modelYear + ".");
    }
}
