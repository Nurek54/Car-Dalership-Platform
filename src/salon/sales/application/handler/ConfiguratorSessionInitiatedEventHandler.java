package salon.sales.application.handler;

import salon.sales.application.domain.event.ConfiguratorSessionInitiatedEvent;
import salon.sales.application.port.out.CatalogIntegration;

/**
 * EVENT HANDLER (Figure 22) — reacts to {@link ConfiguratorSessionInitiatedEvent} (UC-CRM-01):
 * tells the Catalog Context to open the configurator interface.
 */
public class ConfiguratorSessionInitiatedEventHandler {

    private final CatalogIntegration catalogPort;

    public ConfiguratorSessionInitiatedEventHandler(CatalogIntegration catalogPort) {
        this.catalogPort = catalogPort;
    }

    public void handle(ConfiguratorSessionInitiatedEvent event) {
        // We instruct the Catalog port to open the interface
        this.catalogPort.openConfiguratorInterface(event.sessionId(), event.customerId(), event.salespersonId());
    }
}
