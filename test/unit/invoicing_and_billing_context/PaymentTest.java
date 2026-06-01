package unit.invoicing_and_billing_context;

import org.junit.jupiter.api.Test;
import salon.billing.domain.model.payment.Payment;
import salon.billing.domain.model.payment.PaymentCategory;
import salon.billing.domain.model.payment.PaymentId;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class PaymentTest {

    @Test
    void shouldCategorizeAsDepositWhenPaymentCoversRequiredAmount() {
        Money requiredDeposit = Money.of(new BigDecimal("5000.00"), "PLN");
        Payment payment = new Payment(
                new PaymentId("PAY-001"),
                new OrderId("ORD-123"),
                Money.of(new BigDecimal("5000.00"), "PLN"));

        payment.categorizePayment(requiredDeposit);

        assertThat(payment.getCategory()).isEqualTo(PaymentCategory.DEPOSIT);
    }

    @Test
    void shouldCategorizeAsAdvanceWhenPaymentIsLessThanRequiredAmount() {
        Money requiredDeposit = Money.of(new BigDecimal("5000.00"), "PLN");
        Payment payment = new Payment(
                new PaymentId("PAY-002"),
                new OrderId("ORD-124"),
                Money.of(new BigDecimal("3000.00"), "PLN"));

        payment.categorizePayment(requiredDeposit);

        assertThat(payment.getCategory()).isEqualTo(PaymentCategory.ADVANCE);
    }
}
