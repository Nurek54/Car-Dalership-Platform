package salon.sales.infrastructure.persistence;

import salon.sales.infrastructure.persistence.OrderJpaEntity;
import salon.sales.infrastructure.persistence.OrderJpaRepository;

import salon.shared.infrastructure.persistence.DomainReflection;
import org.springframework.stereotype.Component;
import salon.sales.application.port.out.OrderRepository;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.model.order.CancellationReason;
import salon.sales.domain.model.order.Order;
import salon.sales.domain.model.order.OrderState;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.util.Optional;

/**
 * Adapter sterowany (driven) — persystencja zamówienia (port {@link OrderRepository}).
 *
 * Stan i powód anulowania to wynik reguł agregatu; przy odczycie odtwarzamy je refleksją
 * w infrastrukturze (bez przejścia przez metody mutujące), nie modyfikując kodu Sprzedaży.
 */
@Component
public class OrderDatabaseAdapter implements OrderRepository {

    private final OrderJpaRepository repository;

    public OrderDatabaseAdapter(OrderJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Order order) {
        repository.save(toEntity(order));
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return repository.findById(id.value()).map(this::toDomain);
    }

    private OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.id = order.getId().value();
        entity.sourceOfferId = order.getSourceOfferId().value();
        if (order.getRequiredDeposit() != null) {
            entity.requiredDepositAmount = order.getRequiredDeposit().amount();
            entity.requiredDepositCurrency = order.getRequiredDeposit().currency();
        }
        entity.signatureRef = order.getSignatureRef();
        entity.state = order.getState().name();
        entity.cancellationReason = order.getCancellationReason().name();
        return entity;
    }

    private Order toDomain(OrderJpaEntity entity) {
        Money requiredDeposit = entity.requiredDepositAmount == null
                ? null
                : Money.of(entity.requiredDepositAmount, entity.requiredDepositCurrency);
        Order order = new Order(new OrderId(entity.id), new OfferId(entity.sourceOfferId), requiredDeposit);
        if (entity.signatureRef != null) {
            DomainReflection.set(order, "signatureRef", entity.signatureRef);
        }
        DomainReflection.set(order, "state", OrderState.valueOf(entity.state));
        DomainReflection.set(order, "cancellationReason", CancellationReason.valueOf(entity.cancellationReason));
        return order;
    }
}
