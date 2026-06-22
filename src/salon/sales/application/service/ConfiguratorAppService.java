package salon.sales.application.service;

import salon.common.application.EventPublisher;
import salon.sales.application.command.StartConfiguratorSessionCommand;
import salon.sales.application.domain.event.InitiateConfiguratorSessionEvent;
import salon.sales.application.port.in.StartConfigurator;
import salon.sales.application.port.out.CatalogIntegration;

import java.util.UUID;

/**
 * APPLICATION SERVICE (Figure 22) — "ConfiguratorAppService". Realizes the {@link StartConfigurator}
 * inbound port (UC-CRM-01): opens a configurator session, asks the Catalog to open the configurator
 * (CatalogIntegration) and emits InitiateConfiguratorSession.
 */
public class ConfiguratorAppService implements StartConfigurator {

    private final CatalogIntegration catalogIntegration;
    private final EventPublisher eventPublisher;

    public ConfiguratorAppService(CatalogIntegration catalogIntegration, EventPublisher eventPublisher) {
        if (catalogIntegration == null) {
            throw new IllegalArgumentException("catalogIntegration must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        this.catalogIntegration = catalogIntegration;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String startSession(StartConfiguratorSessionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null.");
        }
        String sessionId = "SES-" + UUID.randomUUID();
        this.catalogIntegration.initiateConfiguratorSession(sessionId, command.modelYear());
        this.eventPublisher.publish(new InitiateConfiguratorSessionEvent(
                sessionId, command.customerId(), command.salespersonId(), command.modelYear()));
        return sessionId;
    }
}
