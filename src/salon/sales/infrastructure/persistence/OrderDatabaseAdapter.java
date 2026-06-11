package salon.sales.infrastructure.persistence;

import org.springframework.stereotype.Component;
import salon.sales.application.port.out.OrderRepository;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.model.order.Order;
import salon.sales.domain.model.order.OrderState;
import salon.sales.domain.model.order.PaymentMethod;
import salon.sales.domain.model.order.PaymentStatus;
import salon.shared.infrastructure.persistence.DomainReflection;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Adapter wyjściowy (DatabaseAdapter) portu OrderRepository — mapowanie agregatu Order
 * na model JPA. Odtwarzanie z bazy NIE rejestruje zdarzeń domenowych (omija fabrykę);
 * wersja rekordu (@Version) wędruje z agregatem — zapis nieaktualnej kopii kończy się
 * ObjectOptimisticLockingFailureException (saveAndFlush wymusza weryfikację od razu).
 */
@Component
public class OrderDatabaseAdapter implements OrderRepository {

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
        entity.id = order.getId().value();
        entity.sourceOfferId = order.getOfferId().value();
        if (order.getRequiredDeposit() != null) {
            entity.requiredDepositAmount = order.getRequiredDeposit().amount().toPlainString();
            entity.requiredDepositCurrency = order.getRequiredDeposit().currency();
        }
        entity.paymentMethod = order.getPaymentMethod() == null
                ? null
                : order.getPaymentMethod().name();
        entity.paymentStatus = order.getPaymentStatus() == null
                ? null
                : order.getPaymentStatus().name();
        entity.handoverDate = order.getHandoverDate();
        entity.vehicleId = order.getVehicleId();
        entity.state = order.getState().name();
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
