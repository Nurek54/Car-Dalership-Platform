package salon.sales.application.domain.exception;

/** Naruszenie maszyny stanów oferty (np. akceptacja nieopublikowanej oferty). */
public class InvalidOfferStateException extends RuntimeException {
    public InvalidOfferStateException(String message) {
        super(message);
    }
}
