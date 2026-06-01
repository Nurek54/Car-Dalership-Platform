package unit.invoicing_and_billing_context;

import org.junit.jupiter.api.Test;
import salon.billing.domain.model.settlement.OrderSettlement;
import salon.billing.domain.model.settlement.SettlementId;
import salon.billing.domain.model.settlement.SettlementState;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class OrderSettlementTest {

    @Test
    void shouldCalculateCorrectFinalBalance() {
        OrderSettlement settlement = new OrderSettlement(
                new SettlementId("SET-001"),
                new OrderId("ORD-123"),
                Money.of(new BigDecimal("150000.00"), "PLN"));

        settlement.applyDeposit(Money.of(new BigDecimal("10000.00"), "PLN"));
        settlement.applyFinancing(Money.of(new BigDecimal("100000.00"), "PLN"));
        settlement.applyTradeIn(Money.of(new BigDecimal("30000.00"), "PLN"));

        settlement.calculateBalance();

        assertThat(settlement.getFinalBalance().getAmount()).isEqualByComparingTo("10000.00");
        assertThat(settlement.getState()).isEqualTo(SettlementState.OPEN);
    }

    @Test
    void shouldRequireCorrectionWhenOverpaymentOccurs() {
        OrderSettlement settlement = new OrderSettlement(
                new SettlementId("SET-002"),
                new OrderId("ORD-124"),
                Money.of(new BigDecimal("100000.00"), "PLN"));

        settlement.applyDeposit(Money.of(new BigDecimal("5000.00"), "PLN"));
        settlement.applyFinancing(Money.of(new BigDecimal("50000.00"), "PLN"));
        settlement.applyTradeIn(Money.of(new BigDecimal("60000.00"), "PLN"));

        settlement.calculateBalance();
        settlement.checkForOverpayment();

        assertThat(settlement.getState()).isEqualTo(SettlementState.REQUIRES_CORRECTION);
    }
}
