package salon.sales.application.domain.exception;

/** UC-CRM-03: an offer past its validity date (validityDate) cannot be accepted. */
public class OfferExpiredException extends RuntimeException {
    public OfferExpiredException(String message) {
        super(message);
    }
}
