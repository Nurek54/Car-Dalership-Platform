package salon.sales.application.domain.exception;

/**
 * The requested vehicle specification (or its price) is not available — either the
 * SpecificationCompleted event has not arrived yet, or the Catalog returned 404 (UC-CRM-02).
 */
public class SpecificationNotFoundException extends RuntimeException {

    public SpecificationNotFoundException(String message) {
        super(message);
    }
}
