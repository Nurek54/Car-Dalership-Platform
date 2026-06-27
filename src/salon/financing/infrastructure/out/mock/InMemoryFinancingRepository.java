package salon.financing.infrastructure.out.mock;

import salon.financing.application.domain.model.financing.FinancingApplication;
import salon.financing.application.domain.model.financing.OrderId;
import salon.financing.application.port.out.FinancingApplicationDatabaseRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryFinancingRepository implements FinancingApplicationDatabaseRepository {

    private final Map<String, FinancingApplication> byOrderId = new ConcurrentHashMap<>();

    @Override
    public void save(FinancingApplication application) {
        this.byOrderId.put(application.orderId().value(), application);
    }

    @Override
    public Optional<FinancingApplication> findByOrderId(OrderId orderId) {
        return Optional.ofNullable(this.byOrderId.get(orderId.value()));
    }
}
