package main.java.com.salon.billing.infrastructure.persistence;

import main.java.com.salon.billing.application.port.out.SettlementRepository;
import main.java.com.salon.billing.domain.model.settlement.OrderSettlement;
import main.java.com.salon.billing.domain.model.settlement.SettlementId;

import java.util.Optional;

/**
 * Adapter bazodanowy (DatabaseAdapter) dla agregatu OrderSettlement — SZKIELET pod ORM (JPA).
 */
public class SettlementDatabaseAdapter implements SettlementRepository {

    // TODO: private final SettlementJpaRepository jpaRepository;
    // TODO: private final SettlementMapper mapper;

    @Override
    public void save(OrderSettlement settlement) {
        if (settlement == null) {
            throw new IllegalArgumentException("settlement must not be null.");
        }
        // TODO: jpaRepository.save(mapper.toEntity(settlement));
        throw new UnsupportedOperationException("SettlementDatabaseAdapter.save: not implemented yet (JPA skeleton).");
    }

    @Override
    public Optional<OrderSettlement> findById(SettlementId id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        // TODO: return jpaRepository.findById(id.value()).map(mapper::toDomain);
        throw new UnsupportedOperationException("SettlementDatabaseAdapter.findById: not implemented yet (JPA skeleton).");
    }
}
