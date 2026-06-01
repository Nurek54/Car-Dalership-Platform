package main.java.com.salon.billing.infrastructure.mock;

import main.java.com.salon.billing.application.port.out.PaymentRepository;
import main.java.com.salon.billing.domain.model.payment.Payment;
import main.java.com.salon.billing.domain.model.payment.PaymentId;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repozytorium Payment — MOCK na potrzeby developmentu i testów.
 *
 * Na ten moment dane trzymamy w mapie w pamięci (klucz = UUID z PaymentId).
 * To "mock na JSON": docelowo te wpisy można serializować do pliku .json,
 * a trwałą persystencję bazodanową dostarcza PaymentDatabaseAdapter (ORM/JPA).
 */
public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<UUID, Payment> store = new ConcurrentHashMap<>();

    @Override
    public void save(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("payment must not be null.");
        }
        store.put(payment.getId().value(), payment);
    }

    @Override
    public Optional<Payment> findById(PaymentId id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        return Optional.ofNullable(store.get(id.value()));
    }
}
