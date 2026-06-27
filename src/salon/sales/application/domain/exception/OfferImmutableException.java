package salon.sales.application.domain.exception;

public class OfferImmutableException extends InvalidOfferStateException {

    public OfferImmutableException(String message) {
        super(message);
    }
}
