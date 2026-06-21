package salon.sales.application.domain.exception;

/** An external module (Catalog/Inventory/Accounting) is temporarily unavailable. */
public class ExternalServiceUnavailableException extends RuntimeException {
    public ExternalServiceUnavailableException(String message) {
        super(message);
    }

    public ExternalServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
