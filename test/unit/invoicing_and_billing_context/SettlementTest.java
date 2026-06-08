package unit.invoicing_and_billing_context;

import org.junit.jupiter.api.Test;
import salon.billing.domain.event.SettlementCompletedEvent;
import salon.billing.domain.model.settlement.Settlement;
import salon.billing.domain.model.settlement.SettlementId;
import salon.billing.domain.model.settlement.SettlementStatus;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class SettlementTest {

    @Test
    void shouldStayOpenWithoutPayments() {
        Settlement settlement = new Settlement(
                new SettlementId("SET-001"), new OrderId("ORD-123"),
                Money.of(new BigDecimal("100000.00"), "PLN"));

        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.OPEN);
        assertThat(settlement.getOutstandingBalance().getAmount()).isEqualByComparingTo("100000.00");
    }

    @Test
    void shouldBecomePartialPaymentAfterInsufficientPayment() {
        Settlement settlement = new Settlement(
                new SettlementId("SET-002"), new OrderId("ORD-124"),
                Money.of(new BigDecimal("100000.00"), "PLN"));

        settlement.registerPayment("TX-1", Money.of(new BigDecimal("20000.00"), "PLN"));

        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.PARTIAL_PAYMENT);
        assertThat(settlement.getOutstandingBalance().getAmount()).isEqualByComparingTo("80000.00");
        assertThat(settlement.getPayments()).hasSize(1);
    }

    @Test
    void shouldSettleAndEmitCompletedEventWhenFullyPaid() {
        Settlement settlement = new Settlement(
                new SettlementId("SET-003"), new OrderId("ORD-125"),
                Money.of(new BigDecimal("100000.00"), "PLN"));

        settlement.registerPayment("TX-1", Money.of(new BigDecimal("60000.00"), "PLN"));
        settlement.registerPayment("TX-2", Money.of(new BigDecimal("40000.00"), "PLN"));

        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.SETTLED);
        assertThat(settlement.getOutstandingBalance().getAmount()).isEqualByComparingTo("0.00");
        assertThat(settlement.pullDomainEvents())
                .hasAtLeastOneElementOfType(SettlementCompletedEvent.class);
    }

    @Test
    void shouldRejectPaymentInDifferentCurrency() {
        Settlement settlement = new Settlement(
                new SettlementId("SET-004"), new OrderId("ORD-126"),
                Money.of(new BigDecimal("100000.00"), "PLN"));

        assertThatThrownBy(() ->
                settlement.registerPayment("TX-1", Money.of(new BigDecimal("100.00"), "EUR")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency mismatch");
    }
}
