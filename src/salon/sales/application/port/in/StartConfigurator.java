package salon.sales.application.port.in;

import salon.sales.application.command.StartConfiguratorSessionCommand;

/**
 * INBOUND PORT (Figure 22) — "StartConfigurator".
 * UC-CRM-01: open a new configurator session for a customer; returns the session id.
 */
public interface StartConfigurator {

    String startSession(StartConfiguratorSessionCommand command);
}
