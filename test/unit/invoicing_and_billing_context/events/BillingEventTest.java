package unit.invoicing_and_billing_context.events;

import org.junit.jupiter.api.Test;
import salon.billing.domain.event.AdvancePaymentRequestedEvent;
import salon.billing.domain.event.InvoiceCreatedEvent;
import salon.billing.domain.event.SettlementCompletedEvent;
import salon.billing.domain.model.document.AccountingDocument;
import salon.billing.domain.model.document.BuyerDetails;
import salon.billing.domain.model.document.SellerDetails;
import salon.billing.domain.model.settlement.Settlement;
import salon.billing.domain.model.settlement.SettlementId;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class BillingEventTest {

    @Test
    void shouldEmitAdvancePaymentRequestedEventOnRequest() {
        Settlement settlement = new Settlement(
                new SettlementId("SET-1"), new OrderId("ORD-9"),
                Money.of(new BigDecimal("100000.00"), "PLN"));

        settlement.requestAdvancePayment();

        assertThat(settlement.pullDomainEvents())
                .hasAtLeastOneElementOfType(AdvancePaymentRequestedEvent.class);
    }

    @Test
    void shouldEmitSettlementCompletedEventWhenFullyPaid() {
        Settlement settlement = new Settlement(
                new SettlementId("SET-2"), new OrderId("ORD-10"),
                Money.of(new BigDecimal("1000.00"), "PLN"));

        settlement.registerPayment("TX-1", Money.of(new BigDecimal("1000.00"), "PLN"));

        assertThat(settlement.pullDomainEvents())
                .hasAtLeastOneElementOfType(SettlementCompletedEvent.class);
    }

    @Test
    void shouldEmitInvoiceCreatedEventWhenDocumentCreated() {
        AccountingDocument document = AccountingDocument.createInvoice(
                new OrderId("ORD-11"),
                new BuyerDetails("Jan", "123"),
                new SellerDetails("Salon", "5260000000"),
                Money.of(new BigDecimal("500.00"), "PLN"),
                "Faktura ORD-11",
                "ksiegowy@salon.pl");

        assertThat(document.pullDomainEvents())
                .hasAtLeastOneElementOfType(InvoiceCreatedEvent.class);
    }
}
