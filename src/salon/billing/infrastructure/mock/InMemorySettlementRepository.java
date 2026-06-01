package salon.billing.infrastructure.mock;

import salon.billing.application.port.out.SettlementRepository;
import salon.billing.domain.model.settlement.OrderSettlement;
import salon.billing.domain.model.settlement.SettlementId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemorySettlementRepository implements SettlementRepository {

    private final Map<SettlementId, OrderSettlement> store = new HashMap<>();

    @Override
    public void save(OrderSettlement settlement) {
        this.store.put(settlement.getId(), settlement);
    }

    @Override
    public Optional<OrderSettlement> findById(SettlementId id) {
        return Optional.ofNullable(this.store.get(id));
    }
}
