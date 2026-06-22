package salon.sales.application.port.out;

import salon.common.model.OrderId;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.domain.model.order.Order;

import java.util.List;
import java.util.Optional;

/**
 * OUTBOUND PORT (Figure 22) — "OrderDatabaseRepository". Persistence of the Order aggregate.
 */
public interface OrderDatabaseRepository {

    void save(Order order);

    Optional<Order> findById(OrderId id);

    /** Looks up the order created from a given source offer (UC-CRM-03). */
    Optional<Order> findByOfferId(OfferId offerId);

    List<Order> findAll();
}
