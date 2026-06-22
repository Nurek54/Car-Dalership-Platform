package salon.catalog.application.command;

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
