package unit.billing_context.factories;

import org.junit.jupiter.api.Test;
import salon.billing.application.domain.model.settlement.Settlement;
import salon.billing.application.domain.model.settlement.SettlementFactory;
import salon.billing.application.domain.model.settlement.SettlementStatus;
import salon.common.model.Money;
import salon.common.model.OrderId;

import static org.assertj.core.api.Assertions.*;

/** Fabryka agregatu Settlement — spójny stan początkowy salda (OPEN). */
class SettlementFactoryTest {

    private final SettlementFactory factory = new SettlementFactory();

    @Test
    void shouldCreateOpenSettlement() {
        Settlement settlement = factory.createNew(new OrderId("ORD-1"), Money.of(100000, "PLN"));

        assertThat(settlement.id()).isNotNull();           // fabryka generuje identyfikator
        assertThat(settlement.status()).isEqualTo(SettlementStatus.OPEN);
        assertThat(settlement.totalAmount()).isEqualTo(Money.of(100000, "PLN"));
        assertThat(settlement.outstandingBalance()).isEqualTo(Money.of(100000, "PLN"));
    }
}
