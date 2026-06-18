package salon.logistics.application.domain.exception;

/**
 * UC-INW-02, scenariusz A1: API fabryki zwróciło błąd (np. problem z połączeniem) —
 * zlecenie produkcji nie zostało przyjęte.
 */
public class FactoryOrderRejectedException extends RuntimeException {

    public FactoryOrderRejectedException(String message) {
        super(message);
    }

    public FactoryOrderRejectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
