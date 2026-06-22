package salon.billing.application.domain.model.settlement;

import salon.common.model.Money;
import salon.common.model.OrderId;

public class SettlementFactory {

    public Settlement createNew(OrderId orderId, Money totalAmount) {
        return new Settlement(SettlementId.generate(), orderId, totalAmount, SettlementStatus.OPEN);
    }
}
