package salon.sales.application.domain.exception;

/** Violation of the offer's state machine (e.g. accepting an unpublished offer). */
public class InvalidOfferStateException extends RuntimeException {
    public InvalidOfferStateException(String message) {
        super(message);
    }
}
