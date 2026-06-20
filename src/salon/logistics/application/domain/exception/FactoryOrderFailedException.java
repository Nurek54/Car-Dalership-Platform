package salon.logistics.application.domain.exception;

/**
 * Błąd integracji z systemem fabryki/importera podczas składania zlecenia produkcji
 * (UC-INW-02 / A1) — sygnalizowany przez adapter ACL, tłumaczony przez usługę aplikacji
 * na zdarzenie FactoryOrderFailedEvent.
 */
public class FactoryOrderFailedException extends RuntimeException {

    public FactoryOrderFailedException(String message) {
        super(message);
    }

    public FactoryOrderFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
