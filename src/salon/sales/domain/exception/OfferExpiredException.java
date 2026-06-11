package salon.sales.domain.exception;

/** UC-CRM-03: oferta po terminie ważności (validityDate) nie może zostać zaakceptowana. */
public class OfferExpiredException extends RuntimeException {
    public OfferExpiredException(String message) {
        super(message);
    }
}
