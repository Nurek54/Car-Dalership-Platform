package salon.financing.application.port.out;

import salon.financing.application.domain.model.financing.FinancingApplication;
import salon.financing.application.domain.model.financing.OrderId;

import java.util.Optional;

/**
 * OUTBOUND PORT (Figure 42 — DBAdapter) — persistence of the Financing Application aggregate.
 *
 * The repository provides aggregates of one type (FinancingApplication). Indexed by the identifier
 * the order, because the bank decision (UC-FIN-02) relates to the order. Does not control transactions
 * (that is the application service) and does not create aggregates (that is the factory).
 */
public interface FinancingApplicationDatabaseRepository {

    void save(FinancingApplication application);

    Optional<FinancingApplication> findByOrderId(OrderId orderId);
}
