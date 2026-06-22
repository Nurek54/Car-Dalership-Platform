package salon.sales.application.domain.exception;

/**
 * A persistence-layer failure surfaced to the application layer so the use case can abort
 * the transaction without publishing domain events.
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message) {
        super(message);
    }
}
