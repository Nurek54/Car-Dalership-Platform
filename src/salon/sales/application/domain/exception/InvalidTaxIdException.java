package salon.sales.application.domain.exception;

/**
 * The customer's tax identifier (NIP) does not match the required format (10 digits).
 */
public class InvalidTaxIdException extends RuntimeException {

    public InvalidTaxIdException(String message) {
        super(message);
    }
}
