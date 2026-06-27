package salon.catalog.application.command;

public record FinalizeSpecificationCommand(String specificationId) {

    public FinalizeSpecificationCommand {
        if (specificationId == null || specificationId.isBlank()) {
            throw new IllegalArgumentException("specificationId is required");
        }
    }
}
