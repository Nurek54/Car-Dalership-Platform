package salon.financing.domain.model.insurance;

/**
 * Value Object: numer VIN jako referencja do pojazdu z Inwentarza (model rozłączny).
 * Lokalny dla kontekstu Finansowania — nie współdzielimy klasy między kontekstami.
 */
public record VinNumber(String value) {

    public VinNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("VinNumber must not be blank.");
        }
    }
}
