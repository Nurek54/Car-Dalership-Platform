package integration.driven_adapters.database_adapter.billing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import salon.billing.domain.model.settlement.Settlement;
import salon.billing.domain.model.settlement.SettlementId;
import salon.billing.domain.model.settlement.SettlementStatus;
import salon.billing.infrastructure.persistence.SettlementDatabaseAdapter;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(SettlementDatabaseAdapter.class)
class SettlementDatabaseAdapterTest {

    @Autowired
    private SettlementDatabaseAdapter adapter;

    // 1. ZAPIS, ODCZYT I MAPOWANIE: agregat z kolekcją encji lokalnych (Payment)
    @Test
    void shouldSaveAndRetrieveSettlementWithPaymentsAndStatus() {
        SettlementId settlementId = new SettlementId("SET-2026-001");
        OrderId orderId = new OrderId("ORD-999");

        Settlement settlement = new Settlement(
                settlementId, orderId, Money.of(new BigDecimal("100000.00"), "PLN"));
        settlement.registerPayment("TX-1", Money.of(new BigDecimal("60000.00"), "PLN"));
        settlement.registerPayment("TX-2", Money.of(new BigDecimal("40000.00"), "PLN"));

        adapter.save(settlement);
        Optional<Settlement> retrieved = adapter.findById(settlementId);

        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getId()).isEqualTo(settlementId);
        assertThat(retrieved.get().getOrderId()).isEqualTo(orderId);
        assertThat(retrieved.get().getStatus()).isEqualTo(SettlementStatus.SETTLED);
        assertThat(retrieved.get().getPayments()).hasSize(2);
        assertThat(retrieved.get().getOutstandingBalance().getAmount()).isEqualByComparingTo("0.00");
    }

    // 2. BRAK DANYCH
    @Test
    void shouldReturnEmptyOptionalWhenSettlementDoesNotExist() {
        Optional<Settlement> result = adapter.findById(new SettlementId("SET-UNKNOWN"));
        assertThat(result).isEmpty();
    }
}
