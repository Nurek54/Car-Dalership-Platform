package salon.sales.infrastructure.in.web;

import salon.sales.application.command.StartConfiguratorSessionCommand;
import salon.sales.application.port.in.StartConfigurator;

/**
 * INBOUND ADAPTER (Figure 22: RestController) — drives the {@link StartConfigurator} port (UC-CRM-01).
 * Maps an HTTP request onto a {@link StartConfiguratorSessionCommand} and returns the session id.
 */
public class ConfiguratorRestController {

    private final StartConfigurator startConfigurator;

    public ConfiguratorRestController(StartConfigurator startConfigurator) {
        if (startConfigurator == null) {
            throw new IllegalArgumentException("startConfigurator must not be null.");
        }
        this.startConfigurator = startConfigurator;
    }

    public String startSession(String customerId, String salespersonId, int modelYear) {
        return this.startConfigurator.startSession(
                new StartConfiguratorSessionCommand(customerId, salespersonId, modelYear));
    }
}
