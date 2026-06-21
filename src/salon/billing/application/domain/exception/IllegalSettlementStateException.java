package salon.billing.application.domain.exception;

/**
 * Attempt at an operation not allowed in the current aggregate state (e.g. a payment on a SETTLED balance,
 * issuing a document from outside the DRAFT state) — violation of the state-machine invariant.
 */
public class IllegalSettlementStateException extends RuntimeException {

    public IllegalSettlementStateException(String message) {
        super(message);
    }
}
