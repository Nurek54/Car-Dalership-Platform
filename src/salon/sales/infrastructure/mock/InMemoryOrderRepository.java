package salon.sales.infrastructure.mock;

import salon.sales.application.port.out.OrderRepository;
import salon.sales.domain.model.order.Order;
import salon.shared.model.OrderId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryOrderRepository implements OrderRepository {

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
