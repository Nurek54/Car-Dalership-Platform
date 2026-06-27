package salon.catalog.application.domain.exception;

public class CombinationNotAllowedException extends RuntimeException {

    public CombinationNotAllowedException(String message) {
        super(message);
    }
}
