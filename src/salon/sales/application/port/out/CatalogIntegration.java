package salon.sales.application.port.out;

/**
 * Outbound port (driven) to the Catalog and Configurator Context.
 * UC-CRM-01: opening the configurator interface for the initiated session.
 */
public interface CatalogIntegration {

    void openConfiguratorInterface(String sessionId, String customerId, String salespersonId);
}
