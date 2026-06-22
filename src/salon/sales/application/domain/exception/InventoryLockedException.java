package salon.sales.application.domain.exception;

/**
 * The Inventory/Logistics Context refused to allocate or release a vehicle (e.g. no production
 * slot, vehicle physically blocked) — surfaced from a 409 Conflict on the Inventory API.
 */
public class InventoryLockedException extends RuntimeException {

    public InventoryLockedException(String message) {
        super(message);
    }
}
