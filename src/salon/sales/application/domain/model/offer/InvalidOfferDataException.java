package salon.sales.application.domain.model.offer;

public class InvalidOfferDataException extends RuntimeException {

    public InvalidOfferDataException(String message) {
        super(message);
    }
}
