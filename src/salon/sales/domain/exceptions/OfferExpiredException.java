package salon.sales.domain.exceptions;

/**
 * Wyjatek dziedzinowy: proba konwersji oferty, ktora utracila waznosc (UC-SPR-02).
 */
public class OfferExpiredException extends RuntimeException {
    public OfferExpiredException(String message) {
        super(message);
    }
}
