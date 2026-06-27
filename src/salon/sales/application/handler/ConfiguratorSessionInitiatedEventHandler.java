package salon.sales.application.handler;

import salon.sales.application.domain.event.ConfiguratorSessionInitiatedEvent;
import salon.sales.application.port.out.CatalogIntegration;

public class ConfiguratorSessionInitiatedEventHandler {

    private final CatalogIntegration catalogPort;

    public ConfiguratorSessionInitiatedEventHandler(CatalogIntegration catalogPort) {
        this.catalogPort = catalogPort;
    }

    public void handle(ConfiguratorSessionInitiatedEvent event) {
        
        this.catalogPort.openConfiguratorInterface(event.sessionId(), event.customerId(), event.salespersonId());
    }
}
