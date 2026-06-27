package salon.sales.application.domain.exception;

public class InvalidOfferStateException extends RuntimeException {

    public InvalidOfferStateException(String message) {
        super(message);
    }
}
