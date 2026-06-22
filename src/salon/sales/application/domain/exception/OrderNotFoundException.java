package salon.sales.application.domain.exception;

/**
 * No order with the given identifier exists in the system (UC-CRM-04/05).
 * Mapped by the REST adapter to HTTP 404. The full human-readable message is passed by the caller.
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String message) {
        super(message);
    }
}
