package salon.sales.application.port.in;

import salon.sales.application.command.StartConfiguratorSessionCommand;

public interface StartConfigurator {

    String startSession(StartConfiguratorSessionCommand command);
}
