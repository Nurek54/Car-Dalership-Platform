package salon.billing.application.domain.exception;

/**
 * UC-FIR-02 / A1: nie udalo sie wygenerowac pliku dokumentu (PDF) lub przypisac go do zamowienia.
 */
public class DocumentGenerationException extends RuntimeException {

    public DocumentGenerationException(String message) {
        super(message);
    }

    public DocumentGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
