package salon.logistics.application.domain.exception;

/**
 * An integration error with the factory/importer system while placing the production order
 * (UC-INW-02 / A1) — signaled by the ACL adapter, translated by the application service
 * into the FactoryOrderFailedEvent event.
 */
public class FactoryOrderFailedException extends RuntimeException {

    public FactoryOrderFailedException(String message) {
        super(message);
    }

    public FactoryOrderFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
