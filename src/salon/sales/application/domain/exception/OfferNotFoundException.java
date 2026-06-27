package salon.sales.application.domain.exception;

public class OfferNotFoundException extends RuntimeException {

    public OfferNotFoundException(String offerId) {
        super("Offer " + offerId + " not found in the system");
    }
}
