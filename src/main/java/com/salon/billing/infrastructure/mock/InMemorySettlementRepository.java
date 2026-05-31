package main.java.com.salon.billing.infrastructure.mock;

import main.java.com.salon.billing.application.port.out.SettlementRepository;
import main.java.com.salon.billing.domain.model.settlement.OrderSettlement;
import main.java.com.salon.billing.domain.model.settlement.SettlementId;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repozytorium OrderSettlement — MOCK w pamięci (patrz InMemoryPaymentRepository).
 */
public class InMemorySettlementRepository implements SettlementRepository {

    private final Map<UUID, OrderSettlement> store = new ConcurrentHashMap<>();

    @Override
    public void save(OrderSettlement settlement) {
        if (settlement == null) {
            throw new IllegalArgumentException("settlement must not be null.");
        }
        store.put(settlement.getId().value(), settlement);
    }

    @Override
    public Optional<OrderSettlement> findById(SettlementId id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        return Optional.ofNullable(store.get(id.value()));
    }
}
