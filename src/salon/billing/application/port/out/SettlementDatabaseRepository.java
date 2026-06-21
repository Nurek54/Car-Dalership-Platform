package salon.billing.application.port.out;

import salon.billing.application.domain.model.settlement.Settlement;
import salon.common.model.OrderId;

import java.util.List;
import java.util.Optional;

/**
 * OUTBOUND PORT (Fig. 48 — SettlementDatabaseRepository) — persistence of the Settlement aggregate.
 *
 * The repository provides aggregates of one type (Settlement), creating the illusion of an in-memory collection.
 * Indexed by OrderId, because payments and invoice requests relate to the order. Does not control transactions
 * (that is the application service) and does not create aggregates (that is the factory).
 */
public interface SettlementDatabaseRepository {

    void save(Settlement settlement);

    Optional<Settlement> findByOrderId(OrderId orderId);

    List<Settlement> findAll();
}
