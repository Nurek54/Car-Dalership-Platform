package salon.sales.domain.exception;

/** Brak klienta o wskazanym identyfikatorze w bazie CRM (UC-CRM-01). */
public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
