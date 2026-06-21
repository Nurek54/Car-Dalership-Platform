package salon.sales.application.domain.exception;

/** No order with the indicated identifier in the CRM system. */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
