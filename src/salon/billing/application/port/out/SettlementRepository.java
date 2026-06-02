package salon.billing.application.port.out;

import salon.billing.domain.model.settlement.OrderSettlement;
import salon.billing.domain.model.settlement.SettlementId;

import java.util.Optional;

public interface SettlementRepository {
    void save(OrderSettlement settlement);
    Optional<OrderSettlement> findById(SettlementId id);
}
