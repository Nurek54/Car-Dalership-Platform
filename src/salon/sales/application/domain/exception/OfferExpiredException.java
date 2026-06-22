package salon.sales.application.domain.exception;

/**
 * An attempt to accept a proforma offer whose validity date has already passed (UC-CRM-03).
 */
public class OfferExpiredException extends RuntimeException {

    public OfferExpiredException(String message) {
        super(message);
    }
}
