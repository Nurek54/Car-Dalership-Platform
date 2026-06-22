package salon.sales.application.domain.exception;

/**
 * No customer with the given identifier exists in the CRM master data (UC-CRM-01).
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String message) {
        super(message);
    }
}
