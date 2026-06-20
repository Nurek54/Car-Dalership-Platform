package salon.catalog.application.dto;

/**
 * Polecenie usunięcia wcześniej dobranej opcji ze specyfikacji.
 */
public record RemoveOptionCommand(String specificationId, String optionCode) {

    public RemoveOptionCommand {
        if (specificationId == null || specificationId.isBlank()) {
            throw new IllegalArgumentException("specificationId jest wymagany");
        }
        if (optionCode == null || optionCode.isBlank()) {
            throw new IllegalArgumentException("optionCode jest wymagany");
        }
    }
}
