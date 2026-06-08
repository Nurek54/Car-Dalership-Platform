package salon.billing.infrastructure.mock;

import salon.billing.application.port.out.PaymentRepository;
import salon.billing.domain.model.payment.Payment;
import salon.billing.domain.model.payment.PaymentId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Adapter testowy: trzyma wpłaty w pamięci (zamiast prawdziwej bazy).
public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<PaymentId, Payment> store = new HashMap<>();

    @Override
    public void save(Payment payment) {
        this.store.put(payment.getId(), payment);
    }

    @Override
    public Optional<Payment> findById(PaymentId id) {
        return Optional.ofNullable(this.store.get(id));
    }

    @Override
    public List<Payment> findAll() {
        return new ArrayList<>(this.store.values());
    }
}
