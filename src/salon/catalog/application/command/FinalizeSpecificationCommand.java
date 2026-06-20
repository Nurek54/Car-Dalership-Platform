package salon.catalog.application.command;

/**
 * Polecenie zatwierdzenia specyfikacji (UC-KON-01, krok 5).
 *
 * Należy do warstwy aplikacji (kontrakt portu wejściowego BuildSpecification).
 */
public record FinalizeSpecificationCommand(String specificationId) {

    public FinalizeSpecificationCommand {
        if (specificationId == null || specificationId.isBlank()) {
            throw new IllegalArgumentException("specificationId jest wymagany");
        }
    }
}
