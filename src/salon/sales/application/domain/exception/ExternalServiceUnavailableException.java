package salon.sales.application.domain.exception;

/**
 * An external context/service (Catalog, Inventory, Billing) is unavailable or timed out.
 * The outbound HTTP adapters translate transport errors (5xx, timeouts) into this domain exception.
 */
public class ExternalServiceUnavailableException extends RuntimeException {

    public ExternalServiceUnavailableException(String message) {
        super(message);
    }

    public ExternalServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
