package salon.catalog.application.command;

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
