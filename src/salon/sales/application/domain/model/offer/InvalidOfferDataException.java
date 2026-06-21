package salon.sales.application.domain.model.offer;

/** Violation of the offer's data rules (e.g. a non-positive base price). */
public class InvalidOfferDataException extends RuntimeException {
    public InvalidOfferDataException(String message) {
        super(message);
    }
}
