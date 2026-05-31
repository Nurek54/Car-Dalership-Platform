import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class PaymentTest {

    @Test
    void shouldCategorizeAsDepositWhenPaymentCoversRequiredAmount() {
        // Arrange (Given)
        Money requiredDeposit = Money.of(new BigDecimal("5000.00"), "PLN");

        // Klient wpłaca dokładnie 5000 PLN
        Payment payment = new Payment(
                new PaymentId("PAY-001"),
                new OrderId("ORD-123"),
                Money.of(new BigDecimal("5000.00"), "PLN")
        );

        // Act (When)
        payment.categorizePayment(requiredDeposit);

        // Assert (Then)
        // Zgodnie z wymaganiami, jeśli wpłata w pełni pokrywa wymóg, staje się Zadatkiem (DEPOSIT)[cite: 456].
        assertThat(payment.getCategory()).isEqualTo(PaymentCategory.DEPOSIT);
    }

    @Test
    void shouldCategorizeAsAdvanceWhenPaymentIsLessThanRequiredAmount() {
        // Arrange (Given)
        Money requiredDeposit = Money.of(new BigDecimal("5000.00"), "PLN");

        // Klient wpłaca tylko 3000 PLN
        Payment payment = new Payment(
                new PaymentId("PAY-002"),
                new OrderId("ORD-124"),
                Money.of(new BigDecimal("3000.00"), "PLN")
        );

        // Act (When)
        payment.categorizePayment(requiredDeposit);

        // Assert (Then)
        // Jeśli kwota jest mniejsza, wpłata klasyfikowana jest jako Zaliczka (ADVANCE)[cite: 456].
        assertThat(payment.getCategory()).isEqualTo(PaymentCategory.ADVANCE);
    }
}