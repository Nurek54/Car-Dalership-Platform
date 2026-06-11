package salon.sales.domain.model.offer;

/** Naruszenie reguł danych oferty (np. niedodatnia cena bazowa). */
public class InvalidOfferDataException extends RuntimeException {
    public InvalidOfferDataException(String message) {
        super(message);
    }
}
