package salon.catalog.application.dto;

/**
 * Polecenie dobrania opcji do specyfikacji (UC-KON-01, kroki 2–3).
 * Identyfikatory i kody przekazywane jako proste typy – mapowane na obiekty
 * dziedziny dopiero w usłudze aplikacji.
 */
public record AddOptionCommand(String specificationId, String optionCode) {

    public AddOptionCommand {
        if (specificationId == null || specificationId.isBlank()) {
            throw new IllegalArgumentException("specificationId jest wymagany");
        }
        if (optionCode == null || optionCode.isBlank()) {
            throw new IllegalArgumentException("optionCode jest wymagany");
        }
    }
}
