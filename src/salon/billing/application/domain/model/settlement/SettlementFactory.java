package salon.billing.application.domain.model.settlement;

import salon.common.model.Money;
import salon.common.model.OrderId;

/**
 * FACTORY (Fig. 48 — SettlementFactory): creates a valid {@link Settlement} aggregate
 * in the OPEN state, assigning it a new, global identity ({@link SettlementId}). Creation is not
 * the client's responsibility — the factory guarantees the invariants (complete, non-empty fields) and
 * never returns an object in an inconsistent state.
 */
public class SettlementFactory {

    /** Initializes the balance for an order with a known contract value (UC-CRM-03 -> Billing). */
    public Settlement createNew(OrderId orderId, Money totalAmount) {
        return new Settlement(SettlementId.generate(), orderId, totalAmount, SettlementStatus.OPEN);
    }
}
