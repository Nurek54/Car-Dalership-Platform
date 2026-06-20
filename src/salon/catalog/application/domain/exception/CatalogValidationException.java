package salon.catalog.application.domain.exception;

/**
 * Wyjątek dziedziny – błąd walidacji strukturalnej lub logicznej katalogu
 * (UC-KON-02, scenariusz alternatywny A1: brak cen, niespójne reguły, niezgodny format).
 * Powoduje odrzucenie pakietu i emisję zdarzenia CatalogUpdateFailed.
 */
public class CatalogValidationException extends RuntimeException {

    public CatalogValidationException(String message) {
        super(message);
    }

    public CatalogValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
