package salon.billing.application.domain.exception;

/**
 * UC-FIR-02 / A1: failed to generate the document file (PDF) or assign it to the order.
 */
public class DocumentGenerationException extends RuntimeException {

    public DocumentGenerationException(String message) {
        super(message);
    }

    public DocumentGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
