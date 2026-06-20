package salon.sales.application.domain.exception;

/**
 * Kontekst Inwentarza odmówił operacji na pojeździe (blokada magazynowa) —
 * UC-CRM-05, scenariusz A1.
 */
public class InventoryLockedException extends RuntimeException {
    public InventoryLockedException(String message) {
        super(message);
    }
}
