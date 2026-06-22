package salon.sales.application.domain.exception;

/**
 * No offer with the given identifier exists in the system (UC-CRM-03).
 * Mapped by the REST adapter to HTTP 404.
 */
public class OfferNotFoundException extends RuntimeException {

    public OfferNotFoundException(String offerId) {
        super("Offer " + offerId + " not found in the system");
    }
}
