package salon.sales.application.domain.exception;

/** Failure of the persistence layer (signaled by the database adapter). */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(message);
    }
}
