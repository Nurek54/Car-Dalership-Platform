package salon.billing.infrastructure.mock;

import salon.billing.application.port.out.SettlementRepository;
import salon.billing.domain.model.settlement.Settlement;
import salon.billing.domain.model.settlement.SettlementId;
import salon.shared.model.OrderId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemorySettlementRepository implements SettlementRepository {

    private final Map<SettlementId, Settlement> store = new HashMap<>();

    @Override
    public void save(Settlement settlement) {
        this.store.put(settlement.getId(), settlement);
    }

    @Override
    public Optional<Settlement> findById(SettlementId id) {
        return Optional.ofNullable(this.store.get(id));
    }

    @Override
    public Optional<Settlement> findByOrderId(OrderId orderId) {
        for (Settlement settlement : this.store.values()) {
            if (settlement.getOrderId().equals(orderId)) {
                return Optional.of(settlement);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Settlement> findAll() {
        return new ArrayList<>(this.store.values());
    }
}
