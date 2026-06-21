package salon.catalog.application.command;

/**
 * Input data model (command) for opening a configurator session.
 *
 * SYNTACTIC (non-business) validation happens in the constructor – this is
 * the responsibility of the application layer, not the domain (PDF, chapters 4 and 5).
 */
public record InitiateConfiguratorSessionCommand(int modelYear) {

    public InitiateConfiguratorSessionCommand {
        if (modelYear <= 0) {
            throw new IllegalArgumentException("modelYear must be positive");
        }
    }
}
