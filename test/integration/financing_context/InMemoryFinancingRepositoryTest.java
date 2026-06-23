package integration.financing_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import salon.common.model.Money;
import salon.financing.application.domain.model.financing.ApplicationState;
import salon.financing.application.domain.model.financing.BuyerDetails;
import salon.financing.application.domain.model.financing.CustomerId;
import salon.financing.application.domain.model.financing.FinancingApplication;
import salon.financing.application.domain.model.financing.FinancingApplicationFactory;
import salon.financing.application.domain.model.financing.OrderId;
import salon.financing.application.port.out.FinancingApplicationDatabaseRepository;
import salon.financing.infrastructure.out.mock.InMemoryFinancingRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Integracja adaptera persystencji wniosków o finansowanie (zapis/odczyt po zamówieniu). */
@SpringBootTest(classes = InMemoryFinancingRepository.class)
class InMemoryFinancingRepositoryTest {

    @Autowired private FinancingApplicationDatabaseRepository repository;
    private final FinancingApplicationFactory factory = new FinancingApplicationFactory();

    @Test
    void shouldSaveAndFindByOrderId() {
        FinancingApplication application = factory.createDraft(
                new OrderId("ORD-1"), new CustomerId("CUST-1"),
                new BuyerDetails("Jan Kowalski", "1234563218"), Money.of(100000, "PLN"));
        application.submitApplication(); // PENDING

        // Zapis i odczyt wniosku po identyfikatorze zamówienia
        repository.save(application);
        Optional<FinancingApplication> loaded = repository.findByOrderId(new OrderId("ORD-1"));

        assertThat(loaded).isPresent();
        assertThat(loaded.get().state()).isEqualTo(ApplicationState.PENDING);
        assertThat(repository.findByOrderId(new OrderId("ORD-NONE"))).isEmpty();
    }
}
