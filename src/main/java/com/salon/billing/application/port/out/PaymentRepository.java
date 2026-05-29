package main.java.com.salon.billing.application.port.out;

import main.java.com.salon.billing.domain.model.payment.Payment;
import main.java.com.salon.billing.domain.model.payment.PaymentId;

import java.util.Optional;

// Port wyjściowy (driven port). Adapter (implementacja) trafi do infrastruktury.
public interface PaymentRepository {
    void save(Payment payment);
    Optional<Payment> findById(PaymentId id);
}