package salon.billing.application.port.out;

import salon.billing.application.domain.model.settlement.Settlement;
import salon.billing.application.domain.model.settlement.SettlementId;
import salon.common.model.OrderId;

import java.util.List;
import java.util.Optional;

public interface SettlementDatabaseRepository {
    void save(Settlement settlement);
    Optional<Settlement> findById(SettlementId id);
    Optional<Settlement> findByOrderId(OrderId orderId);
    List<Settlement> findAll();
}
