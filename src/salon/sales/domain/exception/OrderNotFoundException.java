package salon.sales.domain.exception;

/** Brak zamówienia o wskazanym identyfikatorze w systemie CRM. */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
