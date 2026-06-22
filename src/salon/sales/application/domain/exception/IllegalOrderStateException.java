package salon.sales.application.domain.exception;

/**
 * Violation of the Order aggregate's state machine (e.g. confirming handover before it was scheduled).
 */
public class IllegalOrderStateException extends RuntimeException {

    public IllegalOrderStateException(String message) {
        super(message);
    }
}
