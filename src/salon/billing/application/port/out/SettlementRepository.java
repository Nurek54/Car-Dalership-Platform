package salon.billing.application.port.out;

import salon.billing.domain.model.settlement.Settlement;
import salon.billing.domain.model.settlement.SettlementId;
import salon.shared.model.OrderId;

import java.util.List;
import java.util.Optional;

public interface SettlementRepository {
    void save(Settlement settlement);
    Optional<Settlement> findById(SettlementId id);
    Optional<Settlement> findByOrderId(OrderId orderId);
    List<Settlement> findAll();
}
