package salon.sales.application.domain.exception;

public class InvalidTaxIdException extends RuntimeException {

    public InvalidTaxIdException(String message) {
        super(message);
    }
}
