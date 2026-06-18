package salon.sales.application.domain.exception;

/** Brak oferty o wskazanym identyfikatorze w systemie CRM. */
public class OfferNotFoundException extends RuntimeException {

    private final String offerId;

    public OfferNotFoundException(String offerId) {
        super("Offer with ID " + offerId + " not found in the system");
        this.offerId = offerId;
    }

    public String getOfferId() {
        return this.offerId;
    }
}
