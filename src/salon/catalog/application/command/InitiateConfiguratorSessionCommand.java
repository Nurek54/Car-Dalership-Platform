package salon.catalog.application.command;

public record InitiateConfiguratorSessionCommand(int modelYear) {

    public InitiateConfiguratorSessionCommand {
        if (modelYear <= 0) {
            throw new IllegalArgumentException("modelYear must be positive");
        }
    }
}
