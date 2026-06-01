package main.java.com.salon.billing.application.port.out;

import main.java.com.salon.billing.domain.model.payment.Payment;
import main.java.com.salon.billing.domain.model.payment.PaymentId;

import java.util.Optional;

/**
 * Port wyjściowy (driven port) dla agregatu Payment.
 * Kontrakt jest neutralny technologicznie (save/findById), dzięki czemu
 * adapter ORM (JPA) z infrastruktury może go zaimplementować bez "brudzenia" domeny.
 */
public interface PaymentRepository {
    void save(Payment payment);
    Optional<Payment> findById(PaymentId id);
}
