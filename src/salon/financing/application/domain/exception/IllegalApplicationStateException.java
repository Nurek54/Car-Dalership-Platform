package salon.financing.application.domain.exception;

public class IllegalApplicationStateException extends RuntimeException {

    public IllegalApplicationStateException(String message) {
        super(message);
    }
}
