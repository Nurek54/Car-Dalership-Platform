package unit.invoicing_and_billing_context;

import org.junit.jupiter.api.Test;
import salon.billing.domain.model.settlement.Payment;
import salon.shared.model.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class PaymentTest {

    @Test
    void shouldCreateLocalPaymentEntity() {
        LocalDateTime now = LocalDateTime.now();
        Payment payment = new Payment("TX-001", Money.of(new BigDecimal("5000.00"), "PLN"), now);

        assertThat(payment.getTransactionId()).isEqualTo("TX-001");
        assertThat(payment.getAmount().getAmount()).isEqualByComparingTo("5000.00");
        assertThat(payment.getPaymentDate()).isEqualTo(now);
    }

    @Test
    void shouldRejectBlankTransactionId() {
        assertThatThrownBy(() ->
                new Payment("  ", Money.of(new BigDecimal("1.00"), "PLN"), LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("transactionId");
    }
}
