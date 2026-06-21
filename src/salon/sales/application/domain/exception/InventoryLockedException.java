package salon.sales.application.domain.exception;

/**
 * The Inventory Context refused an operation on the vehicle (stock lock) —
 * UC-CRM-05, scenariusz A1.
 */
public class InventoryLockedException extends RuntimeException {
    public InventoryLockedException(String message) {
        super(message);
    }
}
