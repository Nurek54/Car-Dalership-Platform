package salon.catalog.application.domain.exception;

/**
 * Domain exception – structural or logical validation error of the catalog
 * (UC-KON-02, alternative scenario A1: missing prices, inconsistent rules, incompatible format).
 * Causes the package to be rejected and the CatalogUpdateFailed event to be emitted.
 */
public class CatalogValidationException extends RuntimeException {

    public CatalogValidationException(String message) {
        super(message);
    }

    public CatalogValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
