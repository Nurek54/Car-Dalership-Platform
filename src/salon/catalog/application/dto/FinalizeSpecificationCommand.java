package salon.catalog.application.dto;

/**
 * Polecenie zatwierdzenia specyfikacji (UC-KON-01, krok 5).
 */
public record FinalizeSpecificationCommand(String specificationId) {

    public FinalizeSpecificationCommand {
        if (specificationId == null || specificationId.isBlank()) {
            throw new IllegalArgumentException("specificationId jest wymagany");
        }
    }
}
