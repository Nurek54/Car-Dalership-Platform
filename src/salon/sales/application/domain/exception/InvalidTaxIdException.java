package salon.sales.application.domain.exception;

/** Formal customer verification: incorrect tax ID format (the Customer aggregate). */
public class InvalidTaxIdException extends RuntimeException {
    public InvalidTaxIdException(String message) {
        super(message);
    }
}
