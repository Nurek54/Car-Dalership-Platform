package salon.sales.infrastructure.in.web;

import salon.sales.application.command.StartConfiguratorSessionCommand;
import salon.sales.application.port.in.StartConfigurator;

public class ConfiguratorRestController {

    private final StartConfigurator startConfigurator;

    public ConfiguratorRestController(StartConfigurator startConfigurator) {
        if (startConfigurator == null) {
            throw new IllegalArgumentException("startConfigurator must not be null.");
        }
        this.startConfigurator = startConfigurator;
    }

    public String startSession(String customerId, String salespersonId) {
        return this.startConfigurator.startSession(
                new StartConfiguratorSessionCommand(customerId, salespersonId));
    }
}
