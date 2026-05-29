package main.java.com.salon.billing.application.port.out;

import main.java.com.salon.billing.domain.model.settlement.OrderSettlement;
import main.java.com.salon.billing.domain.model.settlement.SettlementId;

import java.util.Optional;

public interface SettlementRepository {
    void save(OrderSettlement settlement);
    Optional<OrderSettlement> findById(SettlementId id);
}