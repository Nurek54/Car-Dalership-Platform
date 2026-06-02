package salon.billing.application.port.out;

import salon.billing.domain.model.payment.Payment;
import salon.billing.domain.model.payment.PaymentId;

import java.util.Optional;

public interface PaymentRepository {
    void save(Payment payment);
    Optional<Payment> findById(PaymentId id);
}
