package salon.catalog.infrastructure.in.event;

import salon.catalog.application.command.InitiateConfiguratorSessionCommand;
import salon.catalog.application.dto.SpecificationView;
import salon.catalog.application.port.in.BuildSpecification;
import org.springframework.stereotype.Component;

/**
 * INBOUND ADAPTER (event-driven) – "EventListener" from the diagram.
 *
 * Listens for the InitiateConfiguratorSession integration event (from the
 * "Sales and CRM" context, OHS mapping) and invokes the inbound port {@link BuildSpecification}.
 * The adapter's tasks: mapping the message to the application service's input data model
 * and invoking the use-case facade. It contains no business logic.
 *
 * In a real integration the method would be annotated, e.g. @RabbitListener(queues = "...").
 */
@Component
public class ConfiguratorEventListener {

    private final BuildSpecification buildSpecification;

    public ConfiguratorEventListener(BuildSpecification buildSpecification) {
        this.buildSpecification = buildSpecification;
    }

    /**
     * Handling of the InitiateConfiguratorSession event.
     *
     * @return a view of the created (working) specification – the session identifier for the UI
     */
    public SpecificationView onInitiateConfiguratorSession(InitiateConfiguratorSessionMessage message) {
        InitiateConfiguratorSessionCommand command =
                new InitiateConfiguratorSessionCommand(message.modelYear());
        return buildSpecification.initiate(command);
    }

    /** Body of the integration event (a minimal set of information). */
    public record InitiateConfiguratorSessionMessage(int modelYear) {
    }
}
