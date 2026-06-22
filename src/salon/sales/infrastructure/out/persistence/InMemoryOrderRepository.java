package salon.sales.infrastructure.out.persistence;

import salon.common.model.OrderId;
import salon.sales.application.domain.model.order.Order;
import salon.sales.application.port.out.OrderDatabaseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOrderRepository implements OrderDatabaseRepository {

    private final Map<String, Order> byId = new ConcurrentHashMap<>();

    @Override
    public void save(Order order) {
        this.byId.put(order.getId().value(), order);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(this.byId.get(id.value()));
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(this.byId.values());
    }
}
