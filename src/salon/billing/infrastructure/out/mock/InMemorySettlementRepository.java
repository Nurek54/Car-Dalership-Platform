package salon.billing.infrastructure.out.mock;

import salon.billing.application.domain.model.settlement.Settlement;
import salon.billing.application.port.out.SettlementDatabaseRepository;
import salon.common.model.OrderId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySettlementRepository implements SettlementDatabaseRepository {

    private final Map<String, Settlement> byOrderId = new ConcurrentHashMap<>();

    @Override
    public void save(Settlement settlement) {
        this.byOrderId.put(settlement.orderId().value(), settlement);
    }

    @Override
    public Optional<Settlement> findByOrderId(OrderId orderId) {
        return Optional.ofNullable(this.byOrderId.get(orderId.value()));
    }

    @Override
    public List<Settlement> findAll() {
        return new ArrayList<>(this.byOrderId.values());
    }
}
