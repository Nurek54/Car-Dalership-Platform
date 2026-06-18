package salon.sales.application.domain.exception;

/** Awaria warstwy persystencji (sygnalizowana przez adapter bazodanowy). */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(message);
    }
}
