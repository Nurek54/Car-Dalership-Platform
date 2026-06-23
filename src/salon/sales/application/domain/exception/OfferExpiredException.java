package salon.sales.application.domain.exception;

public class OfferExpiredException extends RuntimeException {

    public OfferExpiredException(String message) {
        super(message);
    }
}
