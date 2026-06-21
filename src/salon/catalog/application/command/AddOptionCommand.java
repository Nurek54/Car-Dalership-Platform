package salon.catalog.application.command;

/**
 * Command to add an option to the specification (UC-KON-01, steps 2–3).
 * Identifiers and codes passed as simple types – mapped to objects
 * the domain only in the application service.
 *
 * The command belongs to the application layer (the BuildSpecification inbound port contract);
 * the driving adapter builds it from external data and passes it to the port.
 */
public record AddOptionCommand(String specificationId, String optionCode) {

    public AddOptionCommand {
        if (specificationId == null || specificationId.isBlank()) {
            throw new IllegalArgumentException("specificationId is required");
        }
        if (optionCode == null || optionCode.isBlank()) {
            throw new IllegalArgumentException("optionCode is required");
        }
    }
}
