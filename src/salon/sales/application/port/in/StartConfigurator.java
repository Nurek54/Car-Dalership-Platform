package salon.sales.application.port.in;

import salon.sales.application.command.StartConfiguratorSessionCommand;

/**
 * Inbound port for UC-CRM-01 (starting a configurator session) —
 * the "StartConfigurator" node in docs/Architecture/SalesArchitecture.md (PDF chapter 3.3.3).
 *
 * A stateless trigger: it creates no aggregates — it only emits an event
 * that initiates the session in the Catalog and Configurator Context.
 */
public interface StartConfigurator {

    /** Returns the identifier of the newly opened configurator session. */
    String startConfiguratorSession(StartConfiguratorSessionCommand command);
}
