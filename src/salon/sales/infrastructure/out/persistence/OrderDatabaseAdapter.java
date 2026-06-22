package salon.sales.infrastructure.out.persistence;

import org.springframework.stereotype.Repository;
import salon.common.model.Money;
import salon.common.model.OrderId;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.domain.model.order.Order;
import salon.sales.application.port.out.OrderDatabaseRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class OrderDatabaseAdapter implements OrderDatabaseRepository {

    private final OrderJpaRepository jpa;

    public OrderDatabaseAdapter(OrderJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Order order) {
        this.jpa.saveAndFlush(toEntity(order));
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return this.jpa.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<Order> findByOfferId(OfferId offerId) {
        return this.jpa.findBySourceOfferId(offerId.value()).map(this::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return this.jpa.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    private OrderEntity toEntity(Order order) {
        Money deposit = order.requiredDeposit();
        return new OrderEntity(
                order.getId().value(),
                order.getSourceOfferId().value(),
                deposit != null ? deposit.amount() : null,
                deposit != null ? deposit.currency() : null,
                order.getPaymentMethod(), order.paymentStatus(), order.getHandoverDate(),
                order.getState(), order.getVersion());
    }

    private Order toDomain(OrderEntity e) {
        Money deposit = e.getDepositAmount() != null
                ? Money.of(e.getDepositAmount(), e.getDepositCurrency()) : null;
        return Order.reconstitute(
                new OrderId(e.getId()),
                new OfferId(e.getSourceOfferId()),
                deposit, e.getPaymentMethod(), e.getPaymentStatus(),
                e.getHandoverDate(), e.getState(), e.getVersion());
    }
}
