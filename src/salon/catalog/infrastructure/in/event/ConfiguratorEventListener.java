package salon.catalog.infrastructure.in.event;

import salon.catalog.application.command.InitiateConfiguratorSessionCommand;
import salon.catalog.application.dto.SpecificationView;
import salon.catalog.application.port.in.BuildSpecification;
import org.springframework.stereotype.Component;

@Component
public class ConfiguratorEventListener {

    private final BuildSpecification buildSpecification;

    public ConfiguratorEventListener(BuildSpecification buildSpecification) {
        this.buildSpecification = buildSpecification;
    }

    public SpecificationView onInitiateConfiguratorSession(InitiateConfiguratorSessionMessage message) {
        InitiateConfiguratorSessionCommand command =
                new InitiateConfiguratorSessionCommand(message.modelYear());
        return buildSpecification.initiate(command);
    }

    public record InitiateConfiguratorSessionMessage(int modelYear) {
    }
}
