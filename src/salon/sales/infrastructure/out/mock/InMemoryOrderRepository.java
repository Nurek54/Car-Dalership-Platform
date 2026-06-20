package salon.sales.infrastructure.out.mock;

import salon.sales.application.port.out.OrderDatabaseRepository;
import salon.sales.application.domain.model.order.Order;
import salon.common.model.OrderId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryOrderRepository implements OrderDatabaseRepository {

    private final Map<OrderId, Order> store = new HashMap<>();

    @Override
    public void save(Order order) {
        this.store.put(order.getId(), order);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(this.store.get(id));
    }
}
