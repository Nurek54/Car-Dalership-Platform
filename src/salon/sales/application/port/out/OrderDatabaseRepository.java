package salon.sales.application.port.out;

import salon.sales.application.domain.model.order.Order;
import salon.common.model.OrderId;

import java.util.Optional;

public interface OrderDatabaseRepository {
    void save(Order order);
    Optional<Order> findById(OrderId id);
}
