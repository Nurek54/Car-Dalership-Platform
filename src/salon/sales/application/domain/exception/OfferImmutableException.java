package salon.sales.application.domain.exception;

/**
 * An attempt to change the state of an offer that has reached a TERMINAL state (ACCEPTED/REJECTED).
 * Extends {@link InvalidOfferStateException}, so callers that catch the more general state-machine
 * violation still handle it.
 */
public class OfferImmutableException extends InvalidOfferStateException {

    public OfferImmutableException(String message) {
        super(message);
    }
}
