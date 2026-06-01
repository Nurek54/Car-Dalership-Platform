import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class OrderSettlementTest {

    @Test
    void shouldCalculateCorrectFinalBalance() {
        // Arrange (Given)
        OrderSettlement settlement = new OrderSettlement(
                new SettlementId("SET-001"),
                new OrderId("ORD-123"),
                Money.of(new BigDecimal("150000.00"), "PLN") // Wartość pojazdu
        );

        // Dodajemy wpłaty i potrącenia
        settlement.applyDeposit(Money.of(new BigDecimal("10000.00"), "PLN"));
        settlement.applyFinancing(Money.of(new BigDecimal("100000.00"), "PLN"));
        settlement.applyTradeIn(Money.of(new BigDecimal("30000.00"), "PLN"));

        // Act (When)
        settlement.calculateBalance();

        // Assert (Then)
        // 150 000 - 10 000 - 100 000 - 30 000 = 10 000 PLN do zapłaty
        assertThat(settlement.getFinalBalance().getAmount()).isEqualByComparingTo("10000.00");
        assertThat(settlement.getState()).isEqualTo(SettlementState.OPEN);
    }

    @Test
    void shouldRequireCorrectionWhenOverpaymentOccurs() {
        // Arrange (Given)
        OrderSettlement settlement = new OrderSettlement(
                new SettlementId("SET-002"),
                new OrderId("ORD-124"),
                Money.of(new BigDecimal("100000.00"), "PLN") // Wartość pojazdu
        );

        // Klient zapłacił zadatek, dostał finansowanie i oddał drogie auto w rozliczeniu
        settlement.applyDeposit(Money.of(new BigDecimal("5000.00"), "PLN"));
        settlement.applyFinancing(Money.of(new BigDecimal("50000.00"), "PLN"));
        settlement.applyTradeIn(Money.of(new BigDecimal("60000.00"), "PLN")); // Suma potrąceń: 115 000 PLN!

        // Act (When)
        settlement.calculateBalance();
        settlement.checkForOverpayment();

        // Assert (Then)
        // Wystąpienie salda ujemnego musi natychmiast wymusić status REQUIRES_CORRECTION.
        assertThat(settlement.getState()).isEqualTo(SettlementState.REQUIRES_CORRECTION);
    }
}