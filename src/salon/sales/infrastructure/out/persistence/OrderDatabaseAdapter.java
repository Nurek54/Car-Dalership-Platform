package salon.sales.infrastructure.out.persistence;

import org.springframework.stereotype.Component;
import salon.sales.application.port.out.OrderDatabaseRepository;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.domain.model.order.Order;
import salon.sales.application.domain.model.order.OrderState;
import salon.sales.application.domain.model.order.PaymentMethod;
import salon.sales.application.domain.model.order.PaymentStatus;
import salon.common.infrastructure.persistence.DomainReflection;
import salon.common.model.Money;
import salon.common.model.OrderId;
import salon.common.model.SpecificationId;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Adapter wyjściowy (DatabaseAdapter) portu OrderDatabaseRepository — mapowanie agregatu Order
 * na model JPA. Odtwarzanie z bazy NIE rejestruje zdarzeń domenowych (omija fabrykę);
 * wersja rekordu (@Version) wędruje z agregatem — zapis nieaktualnej kopii kończy się
 * ObjectOptimisticLockingFailureException (saveAndFlush wymusza weryfikację od razu).
 */
@Component
public class OrderDatabaseAdapter implements OrderDatabaseRepository {

    private final OrderJpaRepository repository;

    public OrderDatabaseAdapter(OrderJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Order order) {
        repository.saveAndFlush(toEntity(order));
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return repository.findById(id.value()).map(this::toDomain);
    }

    /** Zamówienie utworzone z danej oferty (audytowalność konwersji UC-CRM-03). */
    public Optional<Order> findByOfferId(OfferId offerId) {
        return repository.findBySourceOfferId(offerId.value()).map(this::toDomain);
    }

    private OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.id = order.id().value();
        entity.sourceOfferId = order.offerId().value();
        entity.specificationId = order.specificationId() == null
                ? null
                : order.specificationId().value();
        if (order.requiredDeposit() != null) {
            entity.requiredDepositAmount = order.requiredDeposit().amount().toPlainString();
            entity.requiredDepositCurrency = order.requiredDeposit().currency();
        }
        entity.paymentMethod = order.paymentMethod() == null
                ? null
                : order.paymentMethod().name();
        entity.paymentStatus = order.paymentStatus() == null
                ? null
                : order.paymentStatus().name();
        entity.handoverDate = order.handoverDate();
        entity.vehicleId = order.vehicleId();
        entity.state = order.state().name();
        entity.version = (Long) DomainReflection.get(order, "version");
        return entity;
    }

    private Order toDomain(OrderJpaEntity entity) {
        Money requiredDeposit = entity.requiredDepositAmount == null
                ? null
                : new Money(new BigDecimal(entity.requiredDepositAmount), entity.requiredDepositCurrency);
        Order order = new Order(
                new OrderId(entity.id),
                new OfferId(entity.sourceOfferId),
                entity.specificationId == null ? null : new SpecificationId(entity.specificationId),
                requiredDeposit);
        if (entity.paymentMethod != null) {
            DomainReflection.set(order, "paymentMethod", PaymentMethod.valueOf(entity.paymentMethod));
        }
        if (entity.paymentStatus != null) {
            DomainReflection.set(order, "paymentStatus", PaymentStatus.valueOf(entity.paymentStatus));
        }
        if (entity.handoverDate != null) {
            DomainReflection.set(order, "handoverDate", entity.handoverDate);
        }
        if (entity.vehicleId != null) {
            DomainReflection.set(order, "vehicleId", entity.vehicleId);
        }
        DomainReflection.set(order, "state", OrderState.valueOf(entity.state));
        DomainReflection.set(order, "version", entity.version);
        return order;
    }
}
