package salon.billing.infrastructure.persistence;

import salon.billing.application.port.out.SettlementRepository;
import salon.billing.domain.model.settlement.Settlement;
import salon.billing.domain.model.settlement.SettlementId;
import salon.shared.model.OrderId;

import java.util.List;
import java.util.Optional;

/**
 * Szkielet adaptera bazodanowego (np. JPA). Świadomie pusty — pokazuje, że port
 * SettlementRepository można podmienić na prawdziwą bazę bez dotykania domeny ani aplikacji.
 */
public class SettlementDatabaseAdapter implements SettlementRepository {

    @Override
    public void save(Settlement settlement) {
        throw new UnsupportedOperationException("TODO: implement JPA persistence for Settlement.");
    }

    @Override
    public Optional<Settlement> findById(SettlementId id) {
        throw new UnsupportedOperationException("TODO: implement JPA lookup for Settlement.");
    }

    @Override
    public Optional<Settlement> findByOrderId(OrderId orderId) {
        throw new UnsupportedOperationException("TODO: implement JPA lookup by orderId for Settlement.");
    }

    @Override
    public List<Settlement> findAll() {
        throw new UnsupportedOperationException("TODO: implement JPA listing for Settlement.");
    }
}
