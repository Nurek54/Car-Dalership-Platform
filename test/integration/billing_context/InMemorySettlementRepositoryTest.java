package integration.billing_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import salon.billing.application.domain.model.settlement.Settlement;
import salon.billing.application.domain.model.settlement.SettlementFactory;
import salon.billing.application.port.out.SettlementDatabaseRepository;
import salon.billing.infrastructure.out.mock.InMemorySettlementRepository;
import salon.common.model.Money;
import salon.common.model.OrderId;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Integracja adaptera persystencji rozliczeń (zapis/odczyt salda po zamówieniu). */
@SpringBootTest(classes = InMemorySettlementRepository.class)
class InMemorySettlementRepositoryTest {

    @Autowired private SettlementDatabaseRepository repository;
    private final SettlementFactory factory = new SettlementFactory();

    @Test
    void shouldSaveAndFindByOrderId() {
        Settlement settlement = factory.createNew(new OrderId("ORD-1"), Money.of(100000, "PLN"));

        repository.save(settlement);
        Optional<Settlement> loaded = repository.findByOrderId(new OrderId("ORD-1"));

        assertThat(loaded).isPresent();
        assertThat(loaded.get().totalAmount()).isEqualTo(Money.of(100000, "PLN"));
        assertThat(repository.findByOrderId(new OrderId("ORD-NONE"))).isEmpty();
    }
}
