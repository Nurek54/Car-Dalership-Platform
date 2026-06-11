package salon.sales.application.handler;

import salon.sales.application.port.out.CatalogIntegrationPort;
import salon.sales.domain.event.ConfiguratorSessionInitiatedEvent;

/**
 * Handler zdarzenia ConfiguratorSessionInitiated (UC-CRM-01):
 * zleca Kontekstowi Katalogu otwarcie interfejsu konfiguratora dla sesji klienta.
 */
public class ConfiguratorSessionInitiatedEventHandler {

    private final CatalogIntegrationPort catalogPort;

    public ConfiguratorSessionInitiatedEventHandler(CatalogIntegrationPort catalogPort) {
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
