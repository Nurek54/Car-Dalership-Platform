package salon.sales.application.port.out;

/**
 * OUTBOUND PORT (Figure 22) — "CatalogIntegration" to the Catalog and Configurator Context.
 * UC-CRM-01: opens a configurator session/interface for the salesperson and customer.
 */
public interface CatalogIntegration {

    /** Legacy entry point: open a configurator session for a model year. */
    void initiateConfiguratorSession(String sessionId, int modelYear);

    /** UC-CRM-01: opens the configurator interface for the given session/customer/salesperson. */
    void openConfiguratorInterface(String sessionId, String customerId, String salespersonId);
}
