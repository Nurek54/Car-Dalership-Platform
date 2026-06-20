package salon.financing.application.domain.exception;

/**
 * Naruszenie maszyny stanów agregatu
 * {@link salon.financing.application.domain.model.financing.FinancingApplication}.
 */
public class IllegalApplicationStateException extends RuntimeException {

    public IllegalApplicationStateException(String message) {
        super(message);
    }
}
