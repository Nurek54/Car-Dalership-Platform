package unit.billing_context.domainTests;

import org.junit.jupiter.api.Test;
import salon.billing.application.domain.model.settlement.Settlement;
import salon.billing.application.domain.model.settlement.SettlementFactory;
import salon.billing.application.domain.service.InvoiceCalculationService;
import salon.common.model.Money;
import salon.common.model.OrderId;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/** Serwis domenowy InvoiceCalculationService — matematyka finansowa (zadatek, saldo końcowe). */
class InvoiceCalculationServiceTest {

    private final SettlementFactory factory = new SettlementFactory();
    private final InvoiceCalculationService service = new InvoiceCalculationService();

    @Test
    void shouldCalculateAdvanceAsTenPercent() {
        // UC-FIR-01: zadatek to 10% wartości kontraktu
        Settlement settlement = factory.createNew(new OrderId("ORD-1"), Money.of(100000, "PLN"));

        // Money (rekord) porównuje BigDecimal ze skalą, więc kwotę sprawdzamy numerycznie (10000.00 == 10000)
        assertThat(service.calculateAdvanceAmount(settlement).getAmount()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(service.calculateAdvanceAmount(settlement).currency()).isEqualTo("PLN");
    }

    @Test
    void shouldCalculateFinalAmountAsOutstandingBalance() {
        // UC-FIR-02: faktura końcowa opiewa na kwotę pozostałą do zapłaty
        Settlement settlement = factory.createNew(new OrderId("ORD-2"), Money.of(100000, "PLN"));
        settlement.registerPayment("TX-1", Money.of(10000, "PLN")); // wpłacony zadatek

        assertThat(service.calculateFinalInvoiceAmount(settlement)).isEqualTo(Money.of(90000, "PLN"));
    }
}
