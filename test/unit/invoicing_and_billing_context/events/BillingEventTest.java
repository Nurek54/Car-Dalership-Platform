package unit.invoicing_and_billing_context.events;

import org.junit.jupiter.api.Test;
import salon.billing.domain.event.AdvanceRegisteredEvent;
import salon.billing.domain.event.DepositRegisteredEvent;
import salon.billing.domain.event.InvoiceIssuedEvent;
import salon.billing.domain.model.document.AccountingDocument;
import salon.billing.domain.model.document.DocumentId;
import salon.billing.domain.model.document.DocumentType;
import salon.billing.domain.model.document.TaxDetails;
import salon.billing.domain.model.payment.Payment;
import salon.billing.domain.model.payment.PaymentCategory;
import salon.billing.domain.model.payment.PaymentId;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class BillingEventTest {

    @Test
    void shouldEmitAdvanceRegisteredEventWhenPaymentIsLessThanRequired() {
        // Arrange
        Money requiredDeposit = Money.of(new BigDecimal("5000.00"), "PLN");
        Payment payment = new Payment(
                new PaymentId("PAY-2"), new OrderId("ORD-9"), Money.of(new BigDecimal("1000.00"), "PLN")
        );

        // Act - Klient wpłacił za mało, więc to tylko zaliczka (UC-ROZ-01)
        payment.categorizePayment(requiredDeposit);

        // Assert
        assertThat(payment.getCategory()).isEqualTo(PaymentCategory.ADVANCE);
        assertThat(payment.getDomainEvents())
                .hasAtLeastOneElementOfType(AdvanceRegisteredEvent.class)
                .doesNotHaveAnyElementsOfTypes(DepositRegisteredEvent.class); // Upewniamy się, że nie rzucił złym zdarzeniem!
    }

    @Test
    void shouldEmitInvoiceIssuedEventWhenKsefConfirms() {
        // Arrange
        AccountingDocument document = new AccountingDocument(
                new DocumentId("DOC-1"), DocumentType.VAT_INVOICE, new TaxDetails("Jan", "123")
        );
        document.markAsKsefPending();

        // Act - Zewnętrzny system KSeF zwraca numer (UC-ROZ-02)
        document.confirmKsefRegistration("KSEF-123456789");

        // Assert
        assertThat(document.getDomainEvents())
                .hasAtLeastOneElementOfType(InvoiceIssuedEvent.class);
    }
}