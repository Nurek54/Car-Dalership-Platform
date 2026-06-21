package salon.sales.application.domain.exception;

/**
 * An offer in a terminal state (ACCEPTED/REJECTED) is immutable — it is a closed
 * chapter, a historical record of the negotiated terms (PDF chapter 3.3.4).
 */
public class OfferImmutableException extends InvalidOfferStateException {
    public OfferImmutableException(String message) {
        super(message);
    }
}
