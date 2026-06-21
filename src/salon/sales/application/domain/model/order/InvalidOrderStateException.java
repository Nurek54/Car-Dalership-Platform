package salon.sales.application.domain.model.order;

/** Violation of the order's state machine (e.g. changing the state of a completed order). */
public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(String message) {
        super(message);
    }
}
