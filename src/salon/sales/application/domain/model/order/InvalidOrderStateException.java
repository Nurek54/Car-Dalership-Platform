package salon.sales.application.domain.model.order;

public class InvalidOrderStateException extends IllegalStateException {

    public InvalidOrderStateException(String message) {
        super(message);
    }
}
