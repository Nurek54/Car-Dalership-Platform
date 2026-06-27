package salon.billing.application.domain.exception;

public class IllegalSettlementStateException extends RuntimeException {

    public IllegalSettlementStateException(String message) {
        super(message);
    }
}
