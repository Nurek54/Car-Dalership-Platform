package salon.sales.application.port.out;

import salon.common.model.OrderId;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.domain.model.order.Order;

import java.util.List;
import java.util.Optional;

public interface OrderDatabaseRepository {

    void save(Order order);

    Optional<Order> findById(OrderId id);

    
    Optional<Order> findByOfferId(OfferId offerId);

    List<Order> findAll();
}
