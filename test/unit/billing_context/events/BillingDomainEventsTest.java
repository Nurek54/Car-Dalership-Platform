package unit.billing_context.events;

import org.junit.jupiter.api.Test;
import salon.billing.application.domain.event.AdvancePaymentRegisteredEvent;
import salon.billing.application.domain.event.AdvancePaymentRequestedEvent;
import salon.billing.application.domain.event.ErrorDuringInvoiceCreationEvent;
import salon.billing.application.domain.event.ErrorDuringPaymentRequestEvent;
import salon.billing.application.domain.event.InvoiceCreatedEvent;
import salon.billing.application.domain.event.PaymentRegisteredEvent;
import salon.billing.application.domain.event.SettlementCompletedEvent;

import static org.assertj.core.api.Assertions.*;

/** Zdarzenia dziedzinowe kontekstu Fakturowania i Rozliczeń (payload + automatyczne metadane). */
class BillingDomainEventsTest {

    @Test
    void advancePaymentRequestedShouldCarryOrderId() {
        // UC-FIR-01: kontekst emituje zdarzenie AdvancePaymentRequested
        AdvancePaymentRequestedEvent event = new AdvancePaymentRequestedEvent("ORD-1");
        assertThat(event.orderId()).isEqualTo("ORD-1");
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredOn()).isNotNull();
    }

    @Test
    void advancePaymentRegisteredShouldCarryOrderId() {
        AdvancePaymentRegisteredEvent event = new AdvancePaymentRegisteredEvent("ORD-2");
        assertThat(event.orderId()).isEqualTo("ORD-2");
        assertThat(event.eventId()).isNotNull();
    }

    @Test
    void invoiceCreatedShouldCarryOrderId() {
        // UC-FIR-02: kontekst emituje zdarzenie InvoiceCreated
        InvoiceCreatedEvent event = new InvoiceCreatedEvent("ORD-3");
        assertThat(event.orderId()).isEqualTo("ORD-3");
        assertThat(event.occurredOn()).isNotNull();
    }

    @Test
    void paymentRegisteredAndSettlementCompletedShouldCarryOrderId() {
        // UC-FIR-03: kontekst emituje PaymentRegistered oraz (przy saldzie 0) SettlementCompleted
        PaymentRegisteredEvent payment = new PaymentRegisteredEvent("ORD-4");
        SettlementCompletedEvent completed = new SettlementCompletedEvent("ORD-4");

        assertThat(payment.orderId()).isEqualTo("ORD-4");
        assertThat(completed.orderId()).isEqualTo("ORD-4");
        assertThat(payment.eventId()).isNotNull();
        assertThat(completed.eventId()).isNotNull();
    }

    @Test
    void errorEventsShouldCarryOrderAndReason() {
        // Zdarzenia błędów niosą powód (UC-FIR-01 / A1 oraz UC-FIR-02 / A1)
        ErrorDuringPaymentRequestEvent paymentError =
                new ErrorDuringPaymentRequestEvent("ORD-5", "Brak danych");
        ErrorDuringInvoiceCreationEvent invoiceError =
                new ErrorDuringInvoiceCreationEvent("ORD-6", "Błąd generowania pliku");

        assertThat(paymentError.orderId()).isEqualTo("ORD-5");
        assertThat(paymentError.reason()).isEqualTo("Brak danych");
        assertThat(invoiceError.orderId()).isEqualTo("ORD-6");
        assertThat(invoiceError.reason()).isEqualTo("Błąd generowania pliku");
    }
}
