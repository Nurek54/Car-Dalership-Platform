package salon.sales.application.domain.model.offer;

/**
 * Invalid input data for an {@link Offer} (e.g. a zero or negative base price) — a violation of a
 * basic business rule that the aggregate/factory blocks at creation time.
 */
public class InvalidOfferDataException extends RuntimeException {

    public InvalidOfferDataException(String message) {
        super(message);
    }
}
