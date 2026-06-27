package salon.sales.application.domain.exception;

public class SpecificationNotFoundException extends RuntimeException {

    public SpecificationNotFoundException(String message) {
        super(message);
    }
}
