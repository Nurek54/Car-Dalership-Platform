package salon.financing.application.port.out;

import salon.financing.application.domain.model.financing.FinancingApplication;
import salon.financing.application.domain.model.financing.OrderId;

import java.util.Optional;

public interface FinancingApplicationDatabaseRepository {

    void save(FinancingApplication application);

    Optional<FinancingApplication> findByOrderId(OrderId orderId);
}
