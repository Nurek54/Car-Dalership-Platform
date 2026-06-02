package salon.sales.application.port.out;

import salon.sales.domain.model.order.Order;
import salon.shared.model.OrderId;

import java.util.Optional;

public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(OrderId id);
}
