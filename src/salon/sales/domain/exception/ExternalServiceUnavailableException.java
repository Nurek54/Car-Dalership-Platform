package salon.sales.domain.exception;

/** Zewnętrzny moduł (Katalog/Inwentarz/Księgowość) jest chwilowo niedostępny. */
public class ExternalServiceUnavailableException extends RuntimeException {
    public ExternalServiceUnavailableException(String message) {
        super(message);
    }

    public ExternalServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
