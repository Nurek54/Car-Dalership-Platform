package salon.catalog.application.domain.exception;

/**
 * Wyjątek dziedziny – wybrana kombinacja opcji jest zablokowana przez producenta
 * (UC-KON-01, scenariusz alternatywny A1). Blokuje zatwierdzenie specyfikacji;
 * kontekst NIE emituje wtedy zdarzenia SpecificationCompleted.
 */
public class CombinationNotAllowedException extends RuntimeException {

    public CombinationNotAllowedException(String message) {
        super(message);
    }
}
