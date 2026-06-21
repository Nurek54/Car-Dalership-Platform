package salon.financing.application.domain.exception;

/**
 * No financing application for the indicated order (e.g. a bank decision for an unknown application).
 */
public class FinancingApplicationNotFoundException extends RuntimeException {

    public FinancingApplicationNotFoundException(String message) {
        super(message);
    }
}
