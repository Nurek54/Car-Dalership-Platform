package salon.billing.application.domain.exception;

/**
 * No open balance (Settlement) for the indicated order — e.g. a payment or an invoice request
 * for an order whose settlement has not yet been initialized.
 */
public class SettlementNotFoundException extends RuntimeException {

    public SettlementNotFoundException(String message) {
        super(message);
    }
}
