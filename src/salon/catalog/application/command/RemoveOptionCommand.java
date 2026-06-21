package salon.catalog.application.command;

/**
 * Command to remove a previously added option from the specification.
 *
 * Belongs to the application layer (the BuildSpecification inbound port contract).
 */
public record RemoveOptionCommand(String specificationId, String optionCode) {

    public RemoveOptionCommand {
        if (specificationId == null || specificationId.isBlank()) {
            throw new IllegalArgumentException("specificationId is required");
        }
        if (optionCode == null || optionCode.isBlank()) {
            throw new IllegalArgumentException("optionCode is required");
        }
    }
}
