package salon.billing.infrastructure.out.persistence;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Wpłata jako encja lokalna agregatu Settlement — mapowana jako element kolekcji (@ElementCollection). */
@Embeddable
public class PaymentEmbeddable {

    public String transactionId;
    public BigDecimal amount;
    public String currency;
    public LocalDateTime paymentDate;

    public PaymentEmbeddable() {
    }
}
