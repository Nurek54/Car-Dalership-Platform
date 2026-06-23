package salon.sales.application.domain.exception;

public class IllegalOrderStateException extends RuntimeException {

    public IllegalOrderStateException(String message) {
        super(message);
    }
}
