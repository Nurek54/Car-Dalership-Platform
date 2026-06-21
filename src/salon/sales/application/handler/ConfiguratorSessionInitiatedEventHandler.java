package salon.sales.application.handler;

import salon.sales.application.port.out.CatalogIntegration;
import salon.sales.application.domain.event.ConfiguratorSessionInitiatedEvent;

/**
 * Handler of the ConfiguratorSessionInitiated event (UC-CRM-01):
 * it instructs the Catalog Context to open the configurator interface for the customer's session.
 */
public class ConfiguratorSessionInitiatedEventHandler {

    private final CatalogIntegration catalogPort;

    public ConfiguratorSessionInitiatedEventHandler(CatalogIntegration catalogPort) {
        this.catalogPort = catalogPort;
    }

    public void handle(ConfiguratorSessionInitiatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        this.catalogPort.openConfiguratorInterface(
                event.sessionId(), event.customerId(), event.salespersonId());
    }
}
