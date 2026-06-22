package salon.sales.application.domain.exception;

/**
 * Violation of the Offer aggregate's state machine (e.g. accepting an offer that is not PUBLISHED).
 */
public class InvalidOfferStateException extends RuntimeException {

    public InvalidOfferStateException(String message) {
        super(message);
    }
}
