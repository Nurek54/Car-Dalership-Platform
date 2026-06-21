package salon.financing.application.domain.exception;

/**
 * Violation of the aggregate's state machine
 * {@link salon.financing.application.domain.model.financing.FinancingApplication}.
 */
public class IllegalApplicationStateException extends RuntimeException {

    public IllegalApplicationStateException(String message) {
        super(message);
    }
}
