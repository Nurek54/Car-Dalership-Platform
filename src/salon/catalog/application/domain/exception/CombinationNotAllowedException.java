package salon.catalog.application.domain.exception;

/**
 * Domain exception – the selected combination of options is blocked by the manufacturer
 * (UC-KON-01, alternative scenario A1). Blocks finalization of the specification;
 * the context then does NOT emit the SpecificationCompleted event.
 */
public class CombinationNotAllowedException extends RuntimeException {

    public CombinationNotAllowedException(String message) {
        super(message);
    }
}
