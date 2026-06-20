package salon.financing.infrastructure.out.mock;

import salon.financing.application.domain.model.financing.FinancingApplication;
import salon.financing.application.domain.model.financing.OrderId;
import salon.financing.application.port.out.FinancingApplicationDatabaseRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ADAPTER WYJŚCIOWY (Rysunek 42: DBAdapter) – implementacja
 * {@link FinancingApplicationDatabaseRepository} w pamięci. Indeks po identyfikatorze zamówienia,
 * bo decyzja banku (UC-FIN-02) odnosi się do zamówienia.
 */
public class InMemoryFinancingRepository implements FinancingApplicationDatabaseRepository {

    private final Map<String, FinancingApplication> byOrderId = new ConcurrentHashMap<>();

    @Override
    public void save(FinancingApplication application) {
        this.byOrderId.put(application.getOrderId().value(), application);
    }

    @Override
    public Optional<FinancingApplication> findByOrderId(OrderId orderId) {
        return Optional.ofNullable(this.byOrderId.get(orderId.value()));
    }
}
