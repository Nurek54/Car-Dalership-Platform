package salon.sales.application.domain.exception;

/** No customer with the indicated identifier in the CRM database (UC-CRM-01). */
public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
