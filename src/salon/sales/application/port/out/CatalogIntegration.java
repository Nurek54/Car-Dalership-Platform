package salon.sales.application.port.out;

/**
 * OUTBOUND PORT (Figure 22) — "CatalogIntegration".
 * UC-CRM-01: asks the Catalog and Configurator Context to open a configurator session
 * for the given model year. The adapter (CatalogExternalAPI) handles the technical call.
 */
public interface CatalogIntegration {

    void initiateConfiguratorSession(String sessionId, int modelYear);
}
