package salon.billing.infrastructure.persistence;

import salon.billing.application.port.out.PaymentRepository;
import salon.billing.domain.model.payment.Payment;
import salon.billing.domain.model.payment.PaymentId;

import java.util.Optional;

/**
 * Szkielet adaptera bazodanowego (np. JPA). Świadomie pusty — pokazuje, że port PaymentRepository
 * można podmienić na prawdziwą bazę bez dotykania domeny ani aplikacji.
 */
public class PaymentDatabaseAdapter implements PaymentRepository {

    @Override
    public void save(Payment payment) {
        throw new UnsupportedOperationException("TODO: implement JPA persistence for Payment.");
    }

    @Override
    public Optional<Payment> findById(PaymentId id) {
        throw new UnsupportedOperationException("TODO: implement JPA lookup for Payment.");
    }
}
