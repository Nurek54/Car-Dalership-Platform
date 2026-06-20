package salon.billing.infrastructure.out.persistence;

import org.springframework.stereotype.Component;
import salon.billing.application.port.out.SettlementDatabaseRepository;
import salon.billing.application.domain.model.settlement.Payment;
import salon.billing.application.domain.model.settlement.Settlement;
import salon.billing.application.domain.model.settlement.SettlementId;
import salon.common.model.Money;
import salon.common.model.OrderId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adapter sterowany (driven) — implementacja portu {@link SettlementDatabaseRepository} na JPA (H2/PostgreSQL).
 *
 * Mapuje agregat Settlement na encję JPA i z powrotem. Odtworzenie odbywa się przez ODTWORZENIE
 * przebiegu wpłat (registerPayment), dzięki czemu agregat sam przelicza saldo i status — nie
 * dotykamy jego wewnętrznych pól ani nie modyfikujemy kodu domeny.
 */
@Component
public class SettlementDatabaseAdapter implements SettlementDatabaseRepository {

    private final SettlementJpaRepository repository;

    public SettlementDatabaseAdapter(SettlementJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Settlement settlement) {
        repository.save(toEntity(settlement));
    }

    @Override
    public Optional<Settlement> findById(SettlementId id) {
        return repository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<Settlement> findByOrderId(OrderId orderId) {
        return repository.findByOrderId(orderId.value()).map(this::toDomain);
    }

    @Override
    public List<Settlement> findAll() {
        List<SettlementJpaEntity> entities = repository.findAll();
        List<Settlement> result = new ArrayList<>();
        for (int i = 0; i < entities.size(); i++) {
            result.add(toDomain(entities.get(i)));
        }
        return result;
    }

    private SettlementJpaEntity toEntity(Settlement settlement) {
        SettlementJpaEntity entity = new SettlementJpaEntity();
        entity.id = settlement.getId().value();
        entity.orderId = settlement.getOrderId().value();
        entity.totalAmount = settlement.getTotalAmount().amount();
        entity.currency = settlement.getTotalAmount().currency();
        entity.status = settlement.getStatus().name();
        entity.payments = new ArrayList<>();
        List<Payment> payments = settlement.getPayments();
        for (int i = 0; i < payments.size(); i++) {
            Payment payment = payments.get(i);
            PaymentEmbeddable embeddable = new PaymentEmbeddable();
            embeddable.transactionId = payment.getTransactionId();
            embeddable.amount = payment.getAmount().amount();
            embeddable.currency = payment.getAmount().currency();
            embeddable.paymentDate = payment.getPaymentDate();
            entity.payments.add(embeddable);
        }
        return entity;
    }

    private Settlement toDomain(SettlementJpaEntity entity) {
        Settlement settlement = new Settlement(
                new SettlementId(entity.id),
                new OrderId(entity.orderId),
                Money.of(entity.totalAmount, entity.currency));
        for (int i = 0; i < entity.payments.size(); i++) {
            PaymentEmbeddable payment = entity.payments.get(i);
            settlement.registerPayment(payment.transactionId, Money.of(payment.amount, payment.currency));
        }
        return settlement;
    }
}
