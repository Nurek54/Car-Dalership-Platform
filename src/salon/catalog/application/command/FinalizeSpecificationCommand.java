package salon.catalog.application.command;

/**
 * Command to finalize the specification (UC-KON-01, step 5).
 *
 * Belongs to the application layer (the BuildSpecification inbound port contract).
 */
public record FinalizeSpecificationCommand(String specificationId) {

    public FinalizeSpecificationCommand {
        if (specificationId == null || specificationId.isBlank()) {
            throw new IllegalArgumentException("specificationId is required");
        }
    }
}
