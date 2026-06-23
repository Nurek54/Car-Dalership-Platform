package salon.sales.application.domain.exception;

public class InventoryLockedException extends RuntimeException {

    public InventoryLockedException(String message) {
        super(message);
    }
}
