package main.java.com.salon.billing.infrastructure.persistence;

import main.java.com.salon.billing.application.port.out.PaymentRepository;
import main.java.com.salon.billing.domain.model.payment.Payment;
import main.java.com.salon.billing.domain.model.payment.PaymentId;

import java.util.Optional;

/**
 * Adapter bazodanowy (DatabaseAdapter) dla agregatu Payment — SZKIELET pod ORM (JPA).
 *
 * Zasada (sekcja 3.1): domena jest CZYSTA, więc NIE wstawiamy adnotacji JPA do agregatu Payment.
 * Zamiast tego w tej warstwie żyją OSOBNE encje JPA (np. PaymentJpaEntity z @Entity/@Id)
 * oraz mapper tłumaczący: encja JPA <-> agregat domenowy.
 *
 * Docelowy przepływ:
 *   1. wstrzyknięty PaymentJpaRepository (Spring Data) wykonuje operacje na bazie,
 *   2. mapper zamienia encję JPA na agregat (i odwrotnie),
 *   3. ten adapter implementuje port PaymentRepository, więc warstwa aplikacji nic nie wie o JPA.
 *
 * Na ten moment to SZKIELET — metody nie są jeszcze zaimplementowane.
 */
public class    PaymentDatabaseAdapter implements PaymentRepository {

    // TODO: wstrzyknąć zależności ORM, np.:
    // private final PaymentJpaRepository jpaRepository;
    // private final PaymentMapper mapper;

    @Override
    public void save(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("payment must not be null.");
        }
        // TODO: PaymentJpaEntity entity = mapper.toEntity(payment); jpaRepository.save(entity);
        throw new UnsupportedOperationException("PaymentDatabaseAdapter.save: not implemented yet (JPA skeleton).");
    }

    @Override
    public Optional<Payment> findById(PaymentId id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        // TODO: return jpaRepository.findById(id.value()).map(mapper::toDomain);
        throw new UnsupportedOperationException("PaymentDatabaseAdapter.findById: not implemented yet (JPA skeleton).");
    }
}
