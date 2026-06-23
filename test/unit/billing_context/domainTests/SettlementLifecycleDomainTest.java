package unit.billing_context.domainTests;

import org.junit.jupiter.api.Test;
import salon.billing.application.domain.event.SettlementCompletedEvent;
import salon.billing.application.domain.model.settlement.Settlement;
import salon.billing.application.domain.model.settlement.SettlementFactory;
import salon.billing.application.domain.model.settlement.SettlementStatus;
import salon.common.model.Money;
import salon.common.model.OrderId;

import static org.assertj.core.api.Assertions.*;

/** UC-FIR-03: Pełny cykl rozliczenia salda (na agregacie). */
class SettlementLifecycleDomainTest {

    private final SettlementFactory factory = new SettlementFactory();

    @Test
    void shouldProgressFromOpenThroughPartialToSettled() {
        // OPEN -> (częściowa wpłata) PARTIAL_PAYMENT -> (dopłata) SETTLED
        Settlement settlement = factory.createNew(new OrderId("ORD-1"), Money.of(100000, "PLN"));
        assertThat(settlement.status()).isEqualTo(SettlementStatus.OPEN);

        settlement.registerPayment("TX-1", Money.of(30000, "PLN"));
        assertThat(settlement.status()).isEqualTo(SettlementStatus.PARTIAL_PAYMENT);
        assertThat(settlement.outstandingBalance()).isEqualTo(Money.of(70000, "PLN"));

        settlement.registerPayment("TX-2", Money.of(70000, "PLN"));
        assertThat(settlement.status()).isEqualTo(SettlementStatus.SETTLED);
        assertThat(settlement.outstandingBalance()).isEqualTo(Money.of(0, "PLN"));
        assertThat(settlement.getDomainEvents()).hasAtLeastOneElementOfType(SettlementCompletedEvent.class);
    }
}
