package salon.sales.application.domain.model.order;

/**
 * Violation of the Order aggregate's state machine (e.g. marking a COMPLETED order ready again).
 * Extends {@link IllegalStateException} so callers expecting the standard state-violation type
 * still catch it.
 */
public class InvalidOrderStateException extends IllegalStateException {

    public InvalidOrderStateException(String message) {
        super(message);
    }
}
