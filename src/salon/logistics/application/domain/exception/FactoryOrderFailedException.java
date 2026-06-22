package salon.logistics.application.domain.exception;

public class FactoryOrderFailedException extends RuntimeException {

    public FactoryOrderFailedException(String message) {
        super(message);
    }

    public FactoryOrderFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
