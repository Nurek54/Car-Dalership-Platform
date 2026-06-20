package salon.sales.application.domain.model.order;

/** Naruszenie maszyny stanów zamówienia (np. zmiana stanu zakończonego zamówienia). */
public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(String message) {
        super(message);
    }
}
