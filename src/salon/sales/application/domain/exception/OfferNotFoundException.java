package salon.sales.application.domain.exception;

/** No offer with the indicated identifier in the CRM system. */
public class OfferNotFoundException extends RuntimeException {

    private final String offerId;

    public OfferNotFoundException(String offerId) {
        super("Offer with ID " + offerId + " not found in the system");
        this.offerId = offerId;
    }

    public String offerId() {
        return this.offerId;
    }
}
