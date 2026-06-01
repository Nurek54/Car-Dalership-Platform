package salon.billing.infrastructure.persistence;

import salon.billing.application.port.out.SettlementRepository;
import salon.billing.domain.model.settlement.OrderSettlement;
import salon.billing.domain.model.settlement.SettlementId;

import java.util.Optional;

public class SettlementDatabaseAdapter implements SettlementRepository {

    @Override
    public void save(OrderSettlement settlement) {
        throw new UnsupportedOperationException("TODO: implement JPA persistence for OrderSettlement.");
    }

    @Override
    public Optional<OrderSettlement> findById(SettlementId id) {
        throw new UnsupportedOperationException("TODO: implement JPA lookup for OrderSettlement.");
    }
}
