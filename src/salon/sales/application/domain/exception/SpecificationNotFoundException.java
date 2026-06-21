package salon.sales.application.domain.exception;

/** The Catalog does not know the indicated specification (UC-CRM-02 — pricing impossible). */
public class SpecificationNotFoundException extends RuntimeException {
    public SpecificationNotFoundException(String message) {
        super(message);
    }
}
