package integration.driven_adapters.database_adapter.financing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import salon.financing.domain.model.financing.FinancingApplication;
import salon.financing.domain.model.financing.ApplicationId;
import salon.financing.domain.model.financing.ApplicationState;
import salon.financing.domain.model.financing.FinancingDecision;
import salon.financing.domain.model.financing.DecisionStatus;
import salon.financing.domain.model.financing.CustomerId;
import salon.shared.model.OrderId;
import salon.shared.model.Money;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(FinancingApplicationDatabaseAdapter.class)
class FinancingApplicationDatabaseAdapterTest {

    @Autowired
    private FinancingApplicationDatabaseAdapter adapter;

    // 1. ZAPIS I MAPOWANIE: Zagnieżdżone obiekty (Money i Decyzje Banku)
    @Test
    void shouldSaveAndRetrieveFinancingApplicationWithBankDecision() {
        // Arrange - Tworzymy czysty obiekt dziedziny
        ApplicationId appId = new ApplicationId("APP-2026-001");
        Money requestedAmount = Money.of(new BigDecimal("120000.00"), "PLN");

        FinancingApplication application = new FinancingApplication(
                appId,
                new OrderId("ORD-123"),
                new CustomerId("CUST-99"),
                requestedAmount
        );

        // Symulujemy, że wniosek wrócił z banku z pozytywną decyzją
        FinancingDecision positiveDecision = new FinancingDecision(
                "BANK-REF-777",
                new BigDecimal("120000.00"),
                DecisionStatus.APPROVED
        );
        application.processBankDecision(positiveDecision);

        // Act - Zapis w bazie H2
        adapter.save(application);

        // Odczyt z bazy wymuszający transformację z JPA na Obiekt Dziedziny
        Optional<FinancingApplication> retrievedApp = adapter.findById(appId);

        // Assert - Weryfikacja integralności danych
        assertThat(retrievedApp).isPresent();
        FinancingApplication app = retrievedApp.get();

        assertThat(app.getId()).isEqualTo(appId);
        assertThat(app.getStatus()).isEqualTo(ApplicationState.APPROVED);

        // KLUCZOWE WERYFIKACJE: Czy adapter poprawnie odtworzył klasę Money i obiekt Decyzji?
        assertThat(app.getRequestedAmount().getAmount()).isEqualByComparingTo("120000.00");
        assertThat(app.getRequestedAmount().getCurrency()).isEqualTo("PLN");

        assertThat(app.getBankDecision()).isNotNull();
        assertThat(app.getBankDecision().getBankReference()).isEqualTo("BANK-REF-777");
        assertThat(app.getBankDecision().getStatus()).isEqualTo(DecisionStatus.APPROVED);
    }

    // 2. BRAK DANYCH
    @Test
    void shouldReturnEmptyOptionalWhenFinancingApplicationDoesNotExist() {
        // Act
        Optional<FinancingApplication> result = adapter.findById(new ApplicationId("APP-GHOST"));

        // Assert
        assertThat(result).isEmpty();
    }
}