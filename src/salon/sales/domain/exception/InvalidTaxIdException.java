package salon.sales.domain.exception;

/** Weryfikacja formalna klienta: niepoprawny format numeru NIP (agregat Customer). */
public class InvalidTaxIdException extends RuntimeException {
    public InvalidTaxIdException(String message) {
        super(message);
    }
}
